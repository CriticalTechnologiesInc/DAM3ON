#!/bin/bash
#this script will upload a single file on tahoe grid and will remove the file from the disk
#only metadata of the given file will be left in the repository
#this script will be run from git annex repository where there is already existing remote
# this script will take 2 parameters as input one will be the name of the file to be uploaded and the remote name
file_name=$1
git annex add $file_name
git commit -m "Added $file_name"
remote_name=$2
git annex sync $remote_name --content

#dropping the file from local machine after upload
git annex drop $file_name

git annex whereis $file_name
