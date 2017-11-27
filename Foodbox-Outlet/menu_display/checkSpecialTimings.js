var Q = require('q');
var request = require('request');
var redis = require('redis');

var helper = require('./helper');
// Initiating the redisClient
var redisClient = redis.createClient();
redisClient.on('error', function(msg) {
  console.error(msg);
});

function checkSpecialTimings(outlet_id) {
  var deferred = Q.defer();

  redisClient.get(helper.special_timings_node, function(err, reply) {
    if(err) {
      deferred.reject('error while retreiving from redis- {}'.format(err));
      return;
    }
    deferred.resolve(reply);
  });
  return deferred.promise;

}
module.exports = checkSpecialTimings;
