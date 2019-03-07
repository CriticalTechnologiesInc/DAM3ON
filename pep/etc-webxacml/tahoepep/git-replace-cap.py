#!/usr/bin/env python
import os
import zlib
import sys
import grp,pwd
from datetime import datetime

def encryptCap(plainCap):
    # implement me
    return "nope"

# updates length to be accurate
def updateLength(plainCap):
    #example: blob 1522\0contentoflength17
    c_idx = plainCap.find('\0') # i.e. 9
    content = plainCap[c_idx+1:]
    return "blob " + str(len(content)) + "\0" + content

    
if __name__ == "__main__":

    os.setgid(grp.getgrnam('git').gr_gid) # change uid/gid/$HOME to user git's
    os.setuid(pwd.getpwnam('git').pw_uid) # Apache2 can't do it, so this is run as sudo so it can
    os.environ["HOME"] = "/home/git/" # otherwise, git-annex complains, ableit still works

    try:
        repo = sys.argv[1]
        cap = sys.argv[2]  # plain text capability
    except IndexError:
        print "usage: python git-replace-cap.py <cap>"
        quit()

    log = open("/var/log/tahoe_pep.log", "a+")
    log.write("INFO: About to wipe a capability from existance in " + repo + " @ " + str(datetime.now()) + "\n")
    log.write("DEBUG: Cap being wiped is: " + str(cap) + "\n")
    log.close()

    os.chdir("/opt/git/" + repo)

#    for root, dirs, files in os.walk("/opt/git/Radio_repo/.git/objects/"):
    for root, dirs, files in os.walk("/opt/git/"+repo+"/.git/objects/"):
        path = root.split(os.sep)

        for roott,dirss,filess in os.walk(root):
            for file in files:
                if "idx" in file or "pack" in file:
                    continue
                dc = zlib.decompressobj()
                obj = dc.decompress(open(root + os.sep + file).read())
                if cap in obj:
                    new_obj = obj.replace(cap, encryptCap(cap))  # replace plaintext cap with encrypted cap
                    new_obj = updateLength(new_obj)  # update the length to be accurate
                    os.chmod(root + os.sep + file, 0644)  # objects don't have write permission, so let's give it

                    c = zlib.compressobj()
                    new_obj_c = c.compress(new_obj)
                    new_obj_c += c.flush()

                    object_f = open(root+os.sep+file, 'w') # open object file
                    object_f.write(new_obj_c) # write replacement which contains plain --> cipher
                    object_f.close() # close object

                    os.chmod(root + os.sep + file, 0444)  # revoke write permissions

