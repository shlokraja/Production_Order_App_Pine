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

function populateSpecialTimings() {
  var hq_url = process.env.HQ_URL;
  var outlet_id = process.env.OUTLET_ID;

  request(hq_url + '/outlet/special_timings/' + outlet_id,
    {forever: true},
    function (error, response, body) {
    if (error || (response && response.statusCode != 200)) {
      console.error('{}: {} {}'.format(hq_url, error, body));
      return;
    }


    redisClient.set(helper.special_timings_node, body, function(err, reply){
        if (err) {
          console.error('error while inserting in redis- {}'.format(err));
          return;
        }
    });

  });
}

module.exports = populateSpecialTimings;
