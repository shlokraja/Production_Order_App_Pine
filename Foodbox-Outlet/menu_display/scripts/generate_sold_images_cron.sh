#!/bin/bash
source /opt/foodbox_menu_display/.bootstraprc

if [[ $SOURCE_FOLDER == "" ]]; then
  echo "Source the .bootstraprc file first"
  exit 1
fi

ulimit -n 32768

#Cleanup old sold images
#echo "Cleaning up sold images first"
#find $SOURCE_FOLDER -type f -name '*_sold*.png' -delete

#Create superimposed
echo "Creating superimposed images"
sync
node /opt/foodbox_menu_display/scripts/generateSuperimposed.js /home/atchayam/sold_out.png

