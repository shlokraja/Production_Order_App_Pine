var debug = require('debug')('menu_display');
var format = require('string-format');
var request = require('request');
var redis = require('redis');

var helper = require('./helper');
// Initiating the redisClient
var redisClient = redis.createClient();
redisClient.on('error', function(msg) {
  console.error(msg);
});

function populateExpiryTime() {
  var outlet_id = process.env.OUTLET_ID;
  var hq_url = process.env.HQ_URL;

  request(hq_url + '/food_item/expiry_times/' + outlet_id,
    {forever: true},
    function (error, response, body) {
    if (error || (response && response.statusCode != 200)) {
      console.error('{}: {} {}'.format(hq_url, error, body));
      return;
    }

    // populating the expiry time for every food item id
    var parsed_response = JSON.parse(body);
    for (var i = 0; i < parsed_response.length; i++) {
      var food_item_id = parsed_response[i].id;
      var expiry_time = parsed_response[i].expiry_time;
      redisClient.hset(helper.expiry_time_node, food_item_id, expiry_time, function(err, reply){
          if (err) {
            res.status(500).send('error while inserting in redis- {}'.format(err));
            return;
          }
      });
    }

  });
}

module.exports = populateExpiryTime;
