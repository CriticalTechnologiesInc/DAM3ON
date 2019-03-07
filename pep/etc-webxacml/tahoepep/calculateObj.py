import hashlib
import sys

contents = ""

for line in sys.stdin:
    contents += line

obj = "blob " + str(len(contents)) + "\0" + contents
h = hashlib.sha1(obj)
print h.hexdigest()
