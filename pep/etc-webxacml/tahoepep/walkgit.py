import os
import zlib
import sys

try:
    repo = sys.argv[1]
except:
    print "usage: python walkgit.py <Repo>"
    quit()


if not os.path.isdir("/opt/git/"+repo+"_repo/.git/objects/"):
    print "invalid repo"
    quit()


for root, dirs, files in os.walk("/opt/git/"+repo+"_repo/.git/objects/"):
    path = root.split(os.sep)

    for roott,dirss,filess in os.walk(root):
        for file in files:
            if "idx" in file or "pack" in file:
                continue
            dc = zlib.decompressobj()
            obj = dc.decompress(open(root + os.sep + file).read())
#            if "URI" in obj or "LS0" in obj or "nope" in obj or "blob" in obj:
            if "URI" in obj or "LS0" in obj or "nope" in obj:
                print "Found relevant object located at: " + root + os.sep + file
                print "---------------------------------------------------------"
                print obj
                print "---------------------------------------------------------"
