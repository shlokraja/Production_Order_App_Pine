var debug = require('debug')('menu_display');
var format = require('string-format');
var gm = require('gm');
var fs = require('fs');
var fse = require('fs-extra');
var md5File = require('md5-file');
global.oldAllocations = [123];

format.extend(String.prototype);

// 6 per monitor * 4 monitors
var MAX_ITEM_PER_MONITOR = 6;
var MAX_MONITORS = 4;
var RESOLUTION_MATRIX = {
  '4': [640, 360],
  '5': [[426, 852], 360],
  '6': [[426, 852], 360]
};
var loop_counter = 0;

global.updatebigtv = false;

// getting the source and target folders
var source_folder = process.env.SOURCE_FOLDER;
var target_folder = process.env.TARGET_FOLDER;
var enablesoldout = process.env.ENABLE_SOLD_OUT;

// get the images from the disk and do the image computation and put the
// n no. of images some place
// @response contains info about live and total no. of monitors
// @item_ids contain the total no. of items to be displayed. Its a list of
// dictionaries of format - [{"item_id": 2, "sold": true|false}, {..} ,..]
function generateFinalImage(response, items) {
  var num_ordering_screens = response.num_ordering_screens;
  var num_live_screens = response.num_live_ordering_screens;
  var num_items = items.length;

  // Eliminate sold out items UNLESS the environmental variable
  // ENABLE_SOLDOUT is defined
  if (enablesoldout == undefined){
     var unsolditems = [];
     for (var i = 0; i < num_items; i++){
       	if (items[i].sold == false){
    	    unsolditems.push(items[i]);
        }
     }
	
    items = unsolditems;
    num_items = items.length;
  }

  // Avoid items with broken image-set folders
  var outlet_code = process.env.OUTLET_CODE;
  var unbrokenimages = [];
  for (var i = 0; i < num_items; i++){
    // Convert item-id to master-id
    var master_id = ITEM_TO_MASTER_ID_MAP[items[i].item_id];

    // Do we have outlet-specific folder or global folder for this master id?
    if (fs.existsSync(source_folder + '/' + outlet_code + '/menu_items/' + master_id)
         || fs.existsSync(source_folder + '/' + master_id)
         || master_id == undefined) {
      unbrokenimages.push(items[i]);
    }
    else {
      debug("Skipping due to broken image item id:", items[i].item_id, "master id:", ma$
    }    
  }
  items = unbrokenimages;
  num_items = items.length;

  // check if monitor bounds are okay
  if (num_ordering_screens > MAX_MONITORS) {
    console.error('Max monitors exceeded ! Shortening it to {}'.format(MAX_MONITORS));
    num_ordering_screens = MAX_MONITORS;
  }

  // check if item bounds are okay
  var current_max_items = MAX_ITEM_PER_MONITOR * num_ordering_screens;
  if (num_items > current_max_items) {
    console.error('Max items exceeded ! Shortening the items to {}'.format(current_max_items));
    items = items.slice(0, current_max_items);
    num_items = current_max_items;
  }

  var m = Math.floor(num_items / num_ordering_screens);
  var n = num_items % num_ordering_screens;
  debug('p/q = {}, p%q = {}'.format(m,n));

  // itemAllocation = [list of bucket_items -> list of dicts -> item_id: scale_factor]
  // Allocate the items
  itemAllocation = allocateItems(m, n, items, num_ordering_screens, num_live_screens);

  // Sort the items
  itemAllocation = prioritySort(itemAllocation);

  // Push empty monitors to the end, if any
  var newAllocation = [];
  var emptyMonitor = 0;
  for (i = 0; i < itemAllocation.length; i++) {
    if (itemAllocation[i].length != 0) {
      newAllocation.push(itemAllocation[i]);
    } else {
      emptyMonitor++;
    }
  }
  for (var j = 0; j < emptyMonitor; j++) {
    newAllocation.push([]);
  }
  debug("Item allocation -- ", newAllocation);

  // All monitors are empty, show the empty image set
  if (emptyMonitor == num_live_screens) {
    debug("All monitors empty, showing empty image set");
    for (var i = 0; i < emptyMonitor; i++) {
      (function(i){
      var outlet_code = process.env.OUTLET_CODE;
      fse.copy(source_folder + '/{}/empty/{}.png'.format(outlet_code, i+1),
               target_folder + '/final{}_tmp.png'.format(i), function(err) {
          if(err) {
            debug('custom image not found, copying default image ' +(i));
            fse.copySync(source_folder + '/empty/{}.png'.format(i+1),
               target_folder + '/final{}_tmp.png'.format(i));
            if (md5File(target_folder + '/final' + i + '_tmp.png') !=
                  md5File(target_folder + '/final' + i + '.png')) {
              fse.copySync(target_folder + '/final' +i + '_tmp.png',
                  target_folder + '/final' + i + '.png');
            }
            return;
          }
          if (md5File(target_folder + '/final' + i + '_tmp.png') !=
                md5File(target_folder + '/final' + i + '.png')) {
            fse.copySync(target_folder + '/final' +i + '_tmp.png',
                target_folder + '/final' + i + '.png');
          }
       });
      })(i);
    }
    if (global.oldAllocations != null && global.oldAllocations.length > 0){
        global.oldAllocations = [];
       
        global.updatebigtv = true;
    }
    return;
  }

  // Generate the images
  //XXX: Refactor outside
  for(i = 0; i < newAllocation.length; i++ ) {
    var bucket = newAllocation[i];
    var bucketstring = JSON.stringify(bucket);
    if (bucketstring != global.oldAllocations[i]){
    	global.oldAllocations[i] = bucketstring;
	debug("Stringifying Allocation: " + i + " to " + bucketstring);
        global.updatebigtv = true;
    }
    else{
	debug("Allocation: " + i + " skipping as unchanged");
	continue;
    }

    num_images_in_bucket = bucket.length;
    var final_image = null;
    switch (num_images_in_bucket) {
      case 0:
        debug('Monitor with no images. Showing the empty image');
        fse.copySync(source_folder + '/empty/' + i + '.png',
              target_folder + '/final' + i + '_tmp.png');
        if (md5File(target_folder + '/final' + i + '_tmp.png') !=
                md5File(target_folder + '/final' + i + '.png')) {
          fse.copySync(source_folder + '/empty/' + i + '.png',
              target_folder + '/final' + i + '.png');
        }
        break;
      case 1:
        (function(i){
        var item_id = bucket[0].item_id;
        gm(imagePath(item_id, 1, bucket[0].sold))
          .write(target_folder + '/final' + i + '_tmp.png', function(err) {
            if (err) {
                console.error('Error in writing the image- {}'.format(err));
                return;
            }
            if (md5File(target_folder + '/final' + i + '_tmp.png') !=
                md5File(target_folder + '/final' + i + '.png')) {
              fse.copySync(target_folder + '/final' + i + '_tmp.png',
                  target_folder + '/final' + i + '.png');
              debug('wrote image');
            }
          });
        })(i);
        break;
      case 2:
        (function(i){
        gm(imagePath(bucket[0].item_id, 2, bucket[0].sold))
          .append(imagePath(bucket[1].item_id, 2, bucket[1].sold), true)
          .write(target_folder + '/final' + i + '_tmp.png', function(err) {
            if (err) {
              console.error('Error in writing the image- {}'.format(err));
              return;
            }
            if (md5File(target_folder + '/final' + i + '_tmp.png') !=
                md5File(target_folder + '/final' + i + '.png')) {
              fse.copySync(target_folder + '/final' + i + '_tmp.png',
                  target_folder + '/final' + i + '.png');
              debug('wrote image');
            }
          });
        })(i);
        break;
      case 3:
        (function(i){
        gm(imagePath(bucket[0].item_id, 3, bucket[0].sold))
          .append(imagePath(bucket[1].item_id, 3, bucket[1].sold), true)
          .append(imagePath(bucket[2].item_id, 3, bucket[2].sold), true)
          .write(target_folder + '/final' + i + '_tmp.png', function(err) {
            if (err) {
              console.error('Error in writing the image- {}'.format(err));
              return;
            }
            if (md5File(target_folder + '/final' + i + '_tmp.png') !=
                md5File(target_folder + '/final' + i + '.png')) {
              fse.copySync(target_folder + '/final' + i + '_tmp.png',
                  target_folder + '/final' + i + '.png');
              debug('wrote image');
            }
          });
        })(i);
        break;
      case 4:
        (function(i){
        gm()
        .in('-page', '+0+0')
        .in(imagePath(bucket[0].item_id, 4, bucket[0].sold))
        .in('-page', '+{}+0'.format(RESOLUTION_MATRIX['4'][0]))
        .in(imagePath(bucket[1].item_id, 4, bucket[1].sold))
        .in('-page', '+0+{}'.format(RESOLUTION_MATRIX['4'][1]))
        .in(imagePath(bucket[2].item_id, 4, bucket[2].sold))
        .in('-page', '+{}+{}'.format(RESOLUTION_MATRIX['4'][0],
                                RESOLUTION_MATRIX['4'][1]))
        .in(imagePath(bucket[3].item_id, 4, bucket[3].sold))
        .mosaic()
        .write(target_folder + '/final' + i + '_tmp.png', function(err) {
          if (err) {
          console.error('Error in writing the image- {}'.format(err));
          return;
         }
         if (md5File(target_folder + '/final' + i + '_tmp.png') !=
                md5File(target_folder + '/final' + i + '.png')) {
            fse.copySync(target_folder + '/final' + i + '_tmp.png',
                target_folder + '/final' + i + '.png');
            debug('wrote image');
          }
        });
        })(i);
        break;
      case 5:
        (function(i){
        gm()
        .in('-page', '+0+0')
        .in(imagePath(bucket[0].item_id, 6, bucket[0].sold))
        .in('-page', '+{}+0'.format(RESOLUTION_MATRIX['5'][0][0]))
        .in(imagePath(bucket[1].item_id, 6, bucket[1].sold))
        .in('-page', '+0+{}'.format(RESOLUTION_MATRIX['5'][1]))
        .in(imagePath(bucket[2].item_id, 6, bucket[2].sold))
        .in('-page', '+{}+{}'.format(RESOLUTION_MATRIX['5'][0][0],
                                  RESOLUTION_MATRIX['5'][1]))
        .in(imagePath(bucket[3].item_id, 6, bucket[3].sold))
        .in('-page', '+{}+0'.format(RESOLUTION_MATRIX['5'][0][1]))
        .in(imagePath(bucket[4].item_id, 3, bucket[4].sold))
        .mosaic()
        .write(target_folder + '/final' + i + '_tmp.png', function(err) {
          if (err) {
          console.error('Error in writing the image- {}'.format(err));
          return;
         }
         if (md5File(target_folder + '/final' + i + '_tmp.png') !=
                md5File(target_folder + '/final' + i + '.png')) {
            fse.copySync(target_folder + '/final' + i + '_tmp.png',
                target_folder + '/final' + i + '.png');
            debug('wrote image');
          }
        });
        })(i);
        break;
      case 6:
        (function(i){
        gm()
        .in('-page', '+0+0')
        .in(imagePath(bucket[0].item_id, 6, bucket[0].sold))
        .in('-page', '+{}+0'.format(RESOLUTION_MATRIX['6'][0][0]))
        .in(imagePath(bucket[1].item_id, 6, bucket[1].sold))
        .in('-page', '+0+{}'.format(RESOLUTION_MATRIX['6'][1]))
        .in(imagePath(bucket[3].item_id, 6, bucket[2].sold))
        .in('-page', '+{}+{}'.format(RESOLUTION_MATRIX['6'][0][0],
                                  RESOLUTION_MATRIX['6'][1]))
        .in(imagePath(bucket[4].item_id, 6, bucket[3].sold))
        .in('-page', '+{}+0'.format(RESOLUTION_MATRIX['6'][0][1]))
        .in(imagePath(bucket[2].item_id, 6, bucket[4].sold))
        .in('-page', '+{}+{}'.format(RESOLUTION_MATRIX['6'][0][1],
                                  RESOLUTION_MATRIX['6'][1]))
        .in(imagePath(bucket[5].item_id, 6, bucket[5].sold))
        .mosaic()
        .write(target_folder + '/final' + i + '_tmp.png', function(err) {
          if (err) {
          console.error('Error in writing the image- {}'.format(err));
          return;
         }
         if (md5File(target_folder + '/final' + i + '_tmp.png') !=
                md5File(target_folder + '/final' + i + '.png')) {
            fse.copySync(target_folder + '/final' + i + '_tmp.png',
                  target_folder + '/final' + i + '.png');
            debug('wrote image');
         }
        });
        })(i);
        break;
      default:
        throw 'Number of images in bucket exceeded limit {}'.format(MAX_ITEM_PER_MONITOR);
    }

  }

}

function allocateItems(m, n, items, num_ordering_screens, num_live_screens) {
  currentItemPointer = 0;

  var imageBuckets = new Array(num_ordering_screens);
  // Initially prefill them with m
  for (var i = 0; i < imageBuckets.length; i++) {
    imageBuckets[i] = []
    for (var j = 0; j < m; j++) {
      imageBuckets[i][j] = items[currentItemPointer++];
    }
  }

  // Fill the modulus items
  // from outside first, then inside
  i = 0;
  while(true) {
    if (n==0) {
      break;
    }
    imageBuckets[i].push(items[currentItemPointer++]);
    n--;

    if (n==0) {
      break;
    }
    imageBuckets[imageBuckets.length-1-i].push(items[currentItemPointer++]);
    n--;
    i++;
  }

  var delta_screens = num_ordering_screens - num_live_screens;
  // We need to show a round-robin slideshow in the last monitor
  // This will rotate between the remaining set of images.
  if (delta_screens != 0) {
    var mod_count = loop_counter % (delta_screens+1);
    debug('mod index - {}'.format(mod_count));
    var imageObject = imageBuckets[num_live_screens+mod_count-1];
    // Resizing the image set and putting the 'rotated' object at the end
    imageBuckets = imageBuckets.slice(0,num_live_screens-1);
    imageBuckets.push(imageObject);
    loop_counter++;
    // resetting the loop_counter to 0 when it reaches the max
    if (loop_counter === Number.MAX_VALUE) {
      loop_counter = 0;
    }
    debug('updated image buckets- ', imageBuckets);
  }
  return imageBuckets;
}

// This function will sort the item ids according to specified algorithms
function prioritySort(items) {
  // first flatten the list
  var flattened_list = [];
  items.map(function(bucket) {
    bucket.map(function(item) {
      flattened_list.push(item);
    });
  });

  // sort it according to not-sold and sold out
  // then sort the not-sold according to veg / non-veg
  flattened_list.sort(function(a,b){
    if (a["sold"] != b["sold"]) {
      if (a["sold"] < b["sold"]) {
        return -1;
      } else {
        return 1;
      }
    } else if (a["veg"] != b["veg"]) {
      if (a["veg"] < b["veg"]) {
        return 1;
      } else {
        return -1;
      }
    } else {
      return (Number(a["item_id"]) - Number(b["item_id"]));
    }
  });

  // then put back the items
  var currentIndex = 0;
  items.map(function(bucket, index) {
    var bucket_length = bucket.length;

    // Resetting the bucket
    items[index] = [];
    for (var i = 0; i < bucket_length; i++) {
      items[index].push(flattened_list[currentIndex++]);
    }
  });
  return items;
}

// Helper function to generate the actual image path depending on item id and sold flag
function imagePath(item_id, image_index, sold) {
  item_id = ITEM_TO_MASTER_ID_MAP[item_id];
  var path_str = "";
  var outlet_code = process.env.OUTLET_CODE
  if (fs.existsSync(source_folder + '/' + outlet_code + '/menu_items/' + item_id)) {
    path_str = source_folder + '/' + outlet_code + '/menu_items/' + item_id;
  } else {
    path_str = source_folder + '/' + item_id;
  }
  var sold_suffix = '';
  if (sold) {
    sold_suffix = '_sold';
  }
  path_str += '/{}{}.png'.format(image_index, sold_suffix);
  return path_str;
}

module.exports = generateFinalImage;

