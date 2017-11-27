#!/bin/bash
set -e

#Sourcing the env file
source /opt/foodbox_menu_display/.bootstraprc

#Running the rsync command for all non-outlet folders
rsync -rzvh -e "ssh -p 2225" --exclude $OUTLET_CODE --exclude 'outlets' --delete $REMOTE_SOURCE_FOLDER $SOURCE_FOLDER

tmprem="outlets/"
tmprem="$REMOTE_SOURCE_FOLDER$tmprem$OUTLET_CODE"
tmploc="$SOURCE_FOLDER"

#rsync the outlet specific folder
echo $tmprem
echo $tmploc
rsync -rzvhL -e "ssh -p 2225" --delete $tmprem $tmploc
