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

// Global var that contains the item to master_id map for getting
// the proper folder
ITEM_TO_MASTER_ID_MAP= {};

function populateVegNonveg() {
  var outlet_id = process.env.OUTLET_ID;
  var hq_url = process.env.HQ_URL;

  request(hq_url + '/food_item/veg_nonveg/' + outlet_id,
    {forever: true},
    function (error, response, body) {
    if (error || (response && response.statusCode != 200)) {
      console.error('{}: {} {}'.format(hq_url, error, body));
      return;
    }

    // populating the expiry time for every food item id
    var parsed_response = JSON.parse(body);

    // Create a dict out of it
    var veg_nonveg_dict = {};
    parsed_response.map(function(item) {
      veg_nonveg_dict[item.id] = item.veg;
      ITEM_TO_MASTER_ID_MAP[item.id] = item.master_id;
    });

    redisClient.set(helper.veg_nonveg_node,
      JSON.stringify(veg_nonveg_dict),
      function(err, reply){
        if (err) {
          res.status(500).send('error while inserting in redis- {}'.format(err));
          return;
        }
    });
  });
}

module.exports = populateVegNonveg;
