#!/usr/bin/env python

import os,pwd,grp
import sys
import subprocess

os.setgid(grp.getgrnam('git').gr_gid) # change uid/gid/$HOME to user git's
os.setuid(pwd.getpwnam('git').pw_uid) # Apache2 can't do it, so this is run as sudo so it can
os.environ["HOME"] = "/home/git/" # otherwise, git-annex complains, ableit still works


step = sys.argv[1]
res = sys.argv[2]

if step != "getcap":
    cap = sys.argv[3]
    ex = subprocess.call(['java', '-jar', '/etc/webxacml/tahoepep/tahoepep.jar', step, res, cap])
else:
    ex = subprocess.call(['java', '-jar', '/etc/webxacml/tahoepep/tahoepep.jar', step, res])

sys.exit(ex)
#os.popen("java -jar /etc/webxacml/tahoepep/tahoepep.jar " + res + " " + cap)
