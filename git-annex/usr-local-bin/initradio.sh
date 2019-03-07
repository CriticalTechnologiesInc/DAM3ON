#!/bin/bash

# This script initializes the Tahoe Remote, making it
# available when others clone the repo.
# It calls initremote.sh, which configures your PC's
# instance of Tahoe and restarts tahoe on your PC
# with the correct configuration, not the default.
#
# You run this script only once.
# Use enablestart.sh to start tahoe for future
# use of the Tahoe remote.


# Path and repo
cd /opt/git/Radio_repo

# Full path to git executable, keywords annex and initremote,
# name of Tahoe remote ("Fudge" here), and type of remote
# and whether to allow Tahoe credentials to be put into the
# git repository so that other clones can access them.
/usr/bin/git annex initremote Radio type=tahoe embedcreds=yes

# Full path to initremote.sh executable
# Location of local repo
# Nickname of your PC in Tahoe grid
# Web UI port for Tahoe on your PC - NOT same as Web UI port for Tahoe grid
# Needed - must match Tahoe grid
# Happy - must match Tahoe grid
# Total - must match Tahoe grid
/usr/local/bin/initremote.sh /opt/git/Radio_repo ctidev4 4614 3 4 5

