#!/bin/bash
#this script allows user to download a single file from the connected Tahoe grid
#this script will be called from git annex repository where there is at least one tahoe remote
# created or enabled
#this script will communicate with the tahoe remote and will download the file from tahoe grid
#this script takes one parameter which will be name of the file to be dowloaded
file_name=$1
git annex get $file_name

git annex whereis $file_name
