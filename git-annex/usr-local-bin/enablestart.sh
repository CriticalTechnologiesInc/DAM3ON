#!/bin/bash

# This script enables the Tahoe Remote.
# It calls enableremote.sh, which configures your PC's
# instance of Tahoe and restarts tahoe on your PC
# with the correct configuration, not the default.
#

# Path and repo that you downloaded from server
cd /opt/git/Fudge_repo

# Full path to enableremote.sh executable
# Location of local repo
# Nickname of your PC in Tahoe grid
# Web UI port for Tahoe on your PC
# Needed - must match Tahoe grid
# Happy - must match Tahoe grid
# Total - must match Tahoe grid
/usr/local/sbin/enableremote.sh /opt/git/Fudge_repo ctidev5 4615 3 4 5

