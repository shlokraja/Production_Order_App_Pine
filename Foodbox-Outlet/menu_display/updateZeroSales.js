var debug = require('debug')('menu_display');
var format = require('string-format');
var redis = require('redis');
var async = require('async');

var helper = require('./helper');
// Initiating the redisClient
var redisClient = redis.createClient();
redisClient.on('error', function(msg) {
  console.error(msg);
});

function updateZeroSales() {
  // getting the zero sales list
  redisClient.get(helper.zero_sales_count_node, function(redis_err, redis_res) {
    if(redis_err) {
      console.error(redis_err);
      return;
    }
    var parsed_response = JSON.parse(redis_res);
    var function_list = [];
    for (var item_id in parsed_response) {
      var fn = function(item_id, callback) {
        var current_item_id = item_id;
        // getting the expiry time for the item id
        redisClient.hget(helper.expiry_time_node, current_item_id,
            function(err, expiry_time) {
          if (err) {
            callback('error while retreiving from redis- {}'.format(err), null);
            return;
          }
          if (!expiry_time) {
            callback('expiry time not set', null);
            return;
          }
          var expiry_time_secs = parseInt(expiry_time[0,expiry_time.length-2])*60*60;
          callback(null, [item_id, expiry_time_secs]);
        });
      }
      function_list.push(fn.bind(null, item_id));
    }

    async.parallel(function_list, function(err, expiry_time_list) {
      if (err) {
        console.error(err);
        return;
      }

      var expiry_time_dict = {};
      for (var i = 0; i < expiry_time_list.length; i++) {
        expiry_time_dict[expiry_time_list[i][0]] = expiry_time_list[i][1];
      }

      var current_time = Math.floor(Date.now() / 1000);
      for (var item_id in parsed_response) {
        var item_node = parsed_response[item_id];
        for (var i = 0; i < item_node["item_details"].length; i++) {
          var timestamp = item_node["item_details"][i]["timestamp"];
          if (timestamp + expiry_time_dict[item_id] <= current_time) {
            //item has expired, delete it from the list
            debug('Item id - {} has expired from zero sales'.format(item_id));
            parsed_response[item_id]["item_details"].splice(i,1);
            i--;
          }
        }
        // If there are no more items, then delete the item node
        if (!parsed_response[item_id]["item_details"].length) {
          delete parsed_response[item_id];
        }
      }

      redisClient.set(helper.zero_sales_count_node,
                JSON.stringify(parsed_response),
                function(err, set_zero_sales_reply){
        if (err) {
          console.error('error while inserting in redis- {}'.format(err));
        }
      });
    });

  });
}

module.exports = updateZeroSales;
