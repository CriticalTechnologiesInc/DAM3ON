#!/bin/bash
#this script will upload a all files in current git annex repository on tahoe grid and will remove the files from the disk
#only metadata of the given files will be left in the repository
#this script will be run from git annex repository where there is already existing remote
# this script will take 1 parameter as a input which will be name of the remote we want to sync with
git annex add .
git commit -m "Added all files"
remote_name=$1
git annex sync $remote_name --content

#dropping all the files from local machine after upload
git annex drop *

git annex whereis *
