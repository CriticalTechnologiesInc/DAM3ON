#!/bin/bash
#this script allows user to download a all files related to current topic from the connected Tahoe grid
#this script will be called from git annex repository where there is at least one tahoe remote
# created or enabled
#this script will communicate with the tahoe remote and will download files from tahoe grid
#this script takes one parameter which will be name of the remote to be synced
remote_name=$1
git annex sync $remote_name --content

git annex whereis *
