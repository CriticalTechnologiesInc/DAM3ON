import ntpath
import hashlib
import os
import json
import getpass
import httplib
import sqlite3
import certs_from_input as cfi

username = getpass.getuser()
target_entry = "SABLE-Ubuntu-Linux"
db_api = "ctidev1.critical.com:4653"

manifest_path = "/home/" + username + "/chrome_nm/tpm/sable_manifest.json"
forty_custom = "/usr/local/etc/grub.d/40_custom"
log_path = "/home/" + username + "/tpm/cnm.log"
db_location = "/home/" + username + "/chrome_nm/mod-storage.db"

# FOR TESTING:
#manifest_path = "c:\\users\\jeremy\\google drive\\cti\\darpa\\sable_manifest.json"
#forty_custom = "c:\\users\\jeremy\\google drive\\cti\\darpa\\40_custom"
#log_path = "c:\\users\\jeremy\\desktop\\cti.log"
#db_location = "c:\\users\\jeremy\\desktop\\mod-storage.db"
# END TESTING

def read_40_custom_into_list():
    if not is_filepath_valid(forty_custom):
        return None

    data = []
    with open(forty_custom, 'r') as f:
        flag = False
        for line in f:
            if target_entry.lower() in line.lower() or "sinit" in line.lower():
                flag = True
                continue

            if '}' in line:
                flag = False

            if flag:
                tmp = line.replace("module", "").replace("multiboot", "").strip()
                mod, arg = get_mod_and_arg(tmp)
                data.append({'mod':mod, 'arg':arg})

    return data


def is_filepath_valid(path):
    return os.path.isfile(path)

def getFileNameFromPath(path):
    return ntpath.basename(path)


def get_mod_and_arg(module):
    arg = None
    tmp = module.strip("\n").split(' ', 1)
    mod = tmp[0]
    try:
        arg = tmp[1]
    except IndexError:
        pass
    return mod,arg


def calculate_sha1_hash(fname):
    BUF_SIZE = 65536  # lets read stuff in 64kb chunks!
    sha1 = hashlib.sha1()

    with open(fname, 'rb') as f:
        while True:
            data = f.read(BUF_SIZE)
            if not data:
                break
            sha1.update(data)

    return sha1.hexdigest()

def path_leaf(path):
    head, tail = ntpath.split(path)
    return tail or ntpath.basename(head)

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
            cfi.getCertUrls(listOfMissingCerts, all_files)
            manifestCerts = get_sable_manifest_certs()


        for hash in certsNeeded:
            if hash in manifestCerts:
                idx = hashList.index(hash)
                order = json.loads(json.dumps(all_files.pop(idx)))
                order = order.get("order")
                all_files.insert(idx, {'cert-url': manifestCerts[hash], 'order': order, 'bin-sha1':hash})

        return all_files

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
        #allClientHashes.pop(0) # first hash is the argument for sable. should include? not sure
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

        forty_full_list = read_40_custom_into_list()

        if forty_full_list == None:
            return None

        all_files = []

        #mod,arg = get_mod_and_arg(forty_full_list[0])
        all_files.append({'bin-sha1': hashlib.sha1(str.encode(forty_full_list[0]['arg'])).hexdigest(), 'order': 0, 'arg': forty_full_list[0]['arg'], 'name':"SABLE arg: " + str(path_leaf(forty_full_list[0]['arg']))})
        forty_full_list.pop(0)
        counter = 1

        for patharg in forty_full_list:

            if not is_filepath_valid(patharg['mod']):
                return None
            else:
                all_files.append({'bin-sha1': calculate_sha1_hash(patharg['mod']),
                                  'arg': patharg['arg'], 'name':path_leaf(patharg['mod']), 'order': counter})
                counter += 1

        hashList = [all_files[x].get('bin-sha1') for x in range(0, len(all_files))]
        certsNeeded_l = getCertsNeeded(hashList)

        if certsNeeded_l:
            all_files = addCerts(all_files, certsNeeded_l, hashList)
            return all_files
        else:
            return all_files

    return buildJson()

if __name__ == "__main__":
    thing = file_details()
    print thing
