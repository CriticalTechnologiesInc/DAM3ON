import ntpath
import hashlib
import os
import json
import getpass
import httplib
import sqlite3
import certs_from_input as cfi

username = getpass.getuser()
manifest_path = "/home/" + username + "/chrome_nm/tpm/sable_manifest.json"
forty_custom = "/etc/grub.d/40_custom"
db_api = "ctidev1.critical.com:4653"
log_path = "/home/" + username + "/tpm/cnm.log"
db_location = "/home/" + username + "/chrome_nm/mod-storage.db"

# FOR TESTING:
#manifest_path = "c:\\users\\jeremy\\google drive\\cti\\darpa\\sable_manifest.json"
#forty_custom = "c:\\users\\jeremy\\google drive\\cti\\darpa\\40_custom"
#log_path = "c:\\users\\jeremy\\desktop\\cti.log"
#db_location = "c:\\users\\jeremy\\desktop\\mod-storage.db"
# END TESTING

def is_filepath_valid(path):
    return os.path.isfile(path)


def getFileNameFromPath(path):
    return ntpath.basename(path)


def calculate_sha1_file_hash(fname):
    BUF_SIZE = 65536  # lets read stuff in 64kb chunks!
    sha1 = hashlib.sha1()

    with open(fname, 'rb') as f:
        while True:
            data = f.read(BUF_SIZE)
            if not data:
                break
            sha1.update(data)
    return sha1.hexdigest()


def file_details():

    def get_sable_manifest_certs():
        if not is_filepath_valid(manifest_path):
            return None

        with open(manifest_path) as manifest:
            data = json.load(manifest)

        cert_shas = {}
        for file in data.get('files'):
            if file.get("bin-sha1") is not None:
                cert_shas[file.get("bin-sha1")] = file.get("cert-url")
        return cert_shas

    def addCerts(all_files, certsNeeded, hashList):
        # hashList = list of all hashes in 40 custom
        # manifestCerts = dict of [sha] = certUrl
        # all_files = list of json objects

        manifestCerts = get_sable_manifest_certs()
        listOfMissingCerts = []

        for hash in certsNeeded:
            if hash not in manifestCerts:
                listOfMissingCerts.append(hash)


        if len(listOfMissingCerts) > 0:
            cfi.getCertUrls(listOfMissingCerts)
            manifestCerts = get_sable_manifest_certs()


        for hash in certsNeeded:
            if hash in manifestCerts:
                idx = hashList.index(hash)
                order = json.loads(json.dumps(all_files.pop(idx)))
                order = order.get("order")
                all_files.insert(idx, {'cert-url': manifestCerts[hash], 'order': order, 'bin-sha1':hash})

        return all_files

    def read_40_custom():
        if not is_filepath_valid(forty_custom):
            return None

        data = []
        flag = False

        with open(forty_custom, 'r') as f:
            for line in f:
                if "measure grub" in line.lower():
                    flag = True
                if flag:
                    data.append(line.replace("module", "").lstrip())
                if '}' in line:
                    flag = False

        # yay python
        #      filter removes empty entries
        #      |             remove "}"'s and "\n"'s (which turns blank lines into empty entries)
        #      |             |                             for each 'file' in 'data'
        #      |             |                             |                but not if it contains "menuentry" or "sable.bin"
        return filter(None, [file.replace("}", "").strip().replace("(hd0,msdos1)", "/mnt/BOOT") for file in data if
                             (("menuentry" not in file.lower()) and ("sable-amd" not in file.lower()))])

    def get_40_custom_args_hash():
        if not is_filepath_valid(forty_custom):
            return None

        data = []
        flag = False

        with open(forty_custom, 'r') as f:
            for line in f:
                if "measure grub" in line.lower():
                    flag = True
                if flag:
                    data.append(line.replace("module", "").replace("multiboot", "").lstrip())
                if '}' in line:
                    flag = False

        # yay python
        #      filter removes empty entries
        #      |             remove "}"'s and "\n"'s (which turns blank lines into empty entries)
        #      |             |                             for each 'file' in 'data'
        #      |             |                             |                but not if it contains "menuentry" or "sable.bin"
        entries = filter(None, [file.replace("}", "").strip() for file in data if (("menuentry" not in file.lower()))])
        args = []

        for entry in entries:
            elist = entry.split(None, 1)
            elist.pop(0)
            if len(elist) > 0:
                args.append(elist[0])

        sha1 = hashlib.sha1()
        for arg in args:
            sha1.update(str.encode(arg))
        return sha1.hexdigest()

    # This returns all client hashes that were not in the local database aka ones that need certs
    def checkLocalDbFirst(clientHashes):

        conn = sqlite3.connect(db_location)
        c = conn.cursor()

        # need to make sure the table exists
        c.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='modfiles';")

        if c.fetchone() == None:  # DB didn't exist, so create table and just return
            c.execute("create table modfiles (hash varchar(40));")
            conn.commit()
            conn.close()
            return clientHashes
        else:
            c.execute("select hash from modfiles;")

            hashesInDb = []

            for hash in c.fetchall():
                hashesInDb.append(hash[0])
            conn.close()
            return list(set(clientHashes) - set(hashesInDb))

    def updateLocalDb(hashes_list):
        try:
            conn = sqlite3.connect(db_location)
            c = conn.cursor()

            format_for_db = []
            for hash in hashes_list:
                format_for_db.append((hash,))

            c.executemany("insert into modfiles ('hash') values (?)", format_for_db)
            conn.commit()
            conn.close()
            return True
        except:
            return False

    def getCertsNeeded(allClientHashes):
        # Don't ask for anything that we have previously gotten an answer for
        certsNeeded = checkLocalDbFirst(allClientHashes)
        # then check if clientHashes is still not empty

        payload = "hashes=" + json.dumps(certsNeeded)
        headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}

        conn = httplib.HTTPConnection(db_api)
        conn.request("POST", "/hashcheck", payload, headers)
        resp = conn.getresponse()
        conn.close()
        if resp.status == 200:
            certsNotNeeded = list(json.loads(resp.read()))
            if len(certsNotNeeded) > 0:
                updateLocalDb(certsNotNeeded)
                return list(set(certsNeeded) - set(certsNotNeeded))
            else:
                return certsNeeded
        else:
            return False

    def buildJson():
        filelist = read_40_custom()

        if filelist == None:
            return None

        all_files = []
        counter = 0

        for path in filelist:
            if not is_filepath_valid(path):
                print "invalid filepath"
                return None
            else:
                all_files.append({'bin-sha1': calculate_sha1_file_hash(path),
                                  'order': counter})

                counter += 1

        hashList = [all_files[x].get('bin-sha1') for x in range(0, len(all_files))]
        certsNeeded_l = getCertsNeeded(hashList)

        if certsNeeded_l:
            all_files = addCerts(all_files, certsNeeded_l, hashList)
            all_files.append({'bin-sha1': get_40_custom_args_hash(), 'order': counter})
            return all_files
        else:
            all_files.append({'bin-sha1': get_40_custom_args_hash(), 'order': counter})
            return all_files


    return json.dumps(buildJson()).replace(" ", "")


if __name__ == "__main__":
    thing = file_details()
    print thing
