var request = require('request');
var redis = require('redis');
var debug = require('debug')('menu_display');
var format = require('string-format');
var async = require('async');
var fs = require('fs');
var fse = require('fs-extra');
var path = require('path');
var firebase = require('firebase');
var md5File = require('md5-file');
var g = require('idle-gc');
g.start(2000);

var generateFinalImage = require('./generateFinalImage');
var generateCoalescedImage = require('./generateCoalescedImage');
var checkSpecialTimings = require('./checkSpecialTimings');
var populate_expiry_time = require('./populateExpiryTime');
var populate_num_monitors = require('./populateNumMonitors');
var populate_veg_nonveg = require('./populateVegNonveg');
var populate_special_timings = require('./populateSpecialTimings');
var update_zero_sales = require('./updateZeroSales');
var helper = require('./helper');

format.extend(String.prototype);
// getting the source and target folders
var source_folder = process.env.SOURCE_FOLDER;
var target_folder = process.env.TARGET_FOLDER;
var image_refresh_rate_ms = process.env.IMAGE_REFRESH_RATE_SECS*1000;

// Initiating the redisClient
var redisClient = redis.createClient();
redisClient.on('error', function(msg) {
  console.error(msg);
});

/*
The main function which calls stock_count of outlet_app and gets
the images from HQ and generates the final image
Steps performed -
1. Get stock count data from Outlet
2. Get Zero sales list
3. Get no. of monitors
4. Do the image calculation and generate the final set of images
*/
function main() {
  // do the call to Outlet to get stock count
  var outlet_url = process.env.OUTLET_URL;
  var outlet_code = process.env.OUTLET_CODE;
  var outlet_id = process.env.OUTLET_ID;
  var hq_url = process.env.HQ_URL;

  if (outlet_code.search('/') !== -1) {
    console.error('Invalid value for outlet code- {}'.format(outlet_code));
    return;
  }

  request(outlet_url + '/menu_display/dispenser_status', dispenserStatusCallback);
  function dispenserStatusCallback(error, response, body) {
    if (error || (response && response.statusCode != 200)) {
      console.error('{}: {} {}'.format(outlet_url, error, body));
      // Trying with getting stock count if dispenser status fails
      request(outlet_url + '/menu_display/stock',
        {forever: true},
        receiveStockCountCallback);
      return;
    }
    debug('dispenser status - {}'.format(body));
    if (body === 'empty') {
      // First checking if the current time belongs to a special time slot
      checkSpecialTimings(outlet_id)
        .then(function(slot){
          debug('slot name is - {}'.format(slot));
          if (slot === 'none') {
            // show empty page
            debug('showing empty image');
            redisClient.get(helper.num_monitors_node, function(err, reply) {
              if(err) {
                console.error('error while retreiving from redis- {}'.format(err));
                return;
              }
              parsed_response = JSON.parse(reply);
              debug(parsed_response);
              for (var i = 0; i < parsed_response.num_live_ordering_screens; i++) {
                (function(i){
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
            });
          } else {
            // show the special timing images
            debug('showing the special timing images');
            redisClient.get(helper.num_monitors_node, function(err, reply) {
              if(err) {
                console.error('error while retreiving from redis- {}'.format(err));
                return;
              }
              parsed_response = JSON.parse(reply);
              debug(parsed_response);
              for (var i = 0; i < parsed_response.num_live_ordering_screens; i++) {
                (function(i){
                fse.copy(source_folder + '/{}/special_timings/{}/{}.png'.format(outlet_code, slot, i),
                         target_folder + '/final{}.png'.format(i), function(err) {
                    if(err) {
                      debug('custom image not found, copying default image ' +(i));
                      fse.copySync(source_folder + '/special_timings/{}/{}.png'.format(slot,i),
                         target_folder + '/final{}.png'.format(i));
                    }
                 });
                })(i);
              }
            });
          }
          // Check if prev allocation was non empty, if so empty it and flag bigtv update
          if (global.oldAllocations != null && global.oldAllocations.length > 0){
              debug("previous allocation non-empty, signalling update");
              global.oldAllocations = [];
             
              global.updatebigtv = true;
          }
        },function(error) {
          console.error('Error from HQ - {}'.format(error));
          request(outlet_url + '/menu_display/stock', receiveStockCountCallback);
        });
      return;
    } else {
      request(outlet_url + '/menu_display/stock', receiveStockCountCallback);
    }
  }

  function receiveStockCountCallback(error, response, body) {
    // Returning if there is failure in communication with Outlet
    if (error || (response && response.statusCode != 200)) {
      console.error('{}: {} {}'.format(outlet_url, error, body));
      return;
    }
    debug(body);
    // Returning if the stock data is not yet present
    if (!body) {
      console.log("No stock data yet ..");
      getZeroSales({});
      return;
    }
    parsedResponse = JSON.parse(body);
    getZeroSales(parsedResponse);
  }

  function getZeroSales(items) {
    // get the zero sales list and pass it to getVegNonveg
    redisClient.get(helper.zero_sales_count_node, function(redis_err, redis_res) {
      if(redis_err) {
        console.error(redis_err);
        return;
      }
      var zero_sales_items = JSON.parse(redis_res);
      getVegNonveg(zero_sales_items, items);
    });
  }

  function getVegNonveg(zero_sales_items, stock_items) {
    // get the veg nonveg details and pass it to getNumMonitors
    redisClient.get(helper.veg_nonveg_node, function(redis_err, redis_res) {
      if(redis_err) {
        console.error(redis_err);
        return;
      }
      var veg_nonveg_dict = JSON.parse(redis_res);
      getNumMonitors(zero_sales_items, stock_items, veg_nonveg_dict);
    });
  }

  // Get the no. of monitors from outlet
  function getNumMonitors(zero_sales_items, stock_items, veg_nonveg_dict) {
    var stock_item_ids = [];
    var zero_sales_item_ids = [];
    var all_items = [];
    for (var key in zero_sales_items) {
      zero_sales_item_ids.push(key);
    }
    for (var key in stock_items) {
      var count = getStockItemCount(stock_items[key]["item_details"]);
      // Only adding an item if it hasn't expired
      if (count) {
        stock_item_ids.push(key);
      }
    }

    // getting the diff of zero_sales list to stock_items list
    var sold_item_ids = zero_sales_item_ids.filter(function(elem, pos) {
      return stock_item_ids.indexOf(elem) === -1;
    });

    for (j = 0; j < stock_item_ids.length; j++) {
      if (!veg_nonveg_dict.hasOwnProperty(stock_item_ids[j].toString())) {
        continue;
      }
      all_items.push({"item_id": stock_item_ids[j],
                      "sold": false,
                      "veg": veg_nonveg_dict[stock_item_ids[j].toString()]
                      });
    }
    for (j = 0; j < sold_item_ids.length; j++) {
      if (!veg_nonveg_dict.hasOwnProperty(sold_item_ids[j].toString())) {
        continue;
      }
      all_items.push({"item_id": sold_item_ids[j],
                      "sold": true,
                      "veg": veg_nonveg_dict[sold_item_ids[j].toString()]
                      });
    }

    // After all the callbacks are done
    // get the no. of monitors with the /num_monitors call,
    redisClient.get(helper.num_monitors_node, function(err, reply) {
      if(err) {
        console.error('error while retreiving from redis- {}'.format(err));
        return;
      }
      parsed_response = JSON.parse(reply);
      debug(all_items);
      generateFinalImage(parsed_response, all_items);
    });
  }

}

// Filling up the required data first
populate_expiry_time();
populate_num_monitors();
populate_veg_nonveg();
populate_special_timings();

// The main function callback
// Calling this function after some time to let the expiry times populate
setTimeout(function() {
  setInterval(main, image_refresh_rate_ms);
}, 2000);
// The updation of zero sales callback
setInterval(update_zero_sales, 5000);

setInterval(function() {
  redisClient.get(helper.num_monitors_node, function(err, reply) {
    if(err) {
      console.error('error while retreiving from redis- {}'.format(err));
      return;
    }
    parsed_response = JSON.parse(reply);
    generateCoalescedImage(parsed_response.num_live_ordering_screens);
  });
}, image_refresh_rate_ms);

// setting up a callback for rarely updated information
var expiryTimeRefreshRateMs = process.env.EXPIRY_TIME_REFRESH_RATE_SECS*1000;
setInterval(populate_expiry_time, expiryTimeRefreshRateMs);
setInterval(populate_veg_nonveg, expiryTimeRefreshRateMs);
setInterval(populate_num_monitors, expiryTimeRefreshRateMs);
setInterval(populate_special_timings, expiryTimeRefreshRateMs);

// helper functions
function getStockItemCount(item_details) {
  var count = 0;
  for (var i = 0; i < item_details.length; i++) {
    if (!item_details[i]["expired"] && !item_details[i]["spoiled"]) {
      count += item_details[i]["count"];
    }
  }
  return count;
}
