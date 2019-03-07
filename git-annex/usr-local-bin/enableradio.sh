#!/bin/bash

# This script enables the Tahoe Radio Remote.
# It calls enableremote.sh, which configures your PC's
# instance of Tahoe and restarts tahoe on your PC
# with the correct configuration, not the default.
#

# Path and name of repo
cd /opt/git/Radio_repo

# Full path to enableremote.sh executable
# Location of local repo
# Nickname of your PC in Tahoe grid
# Web UI port for Tahoe/repo on your PC - NOT same as Web UI port for Tahoe grid
# Needed - must match Tahoe grid
# Happy - must match Tahoe grid
# Total - must match Tahoe grid
/usr/local/bin/enableremote.sh /opt/git/Radio_repo ctidev4 4614 3 4 5

