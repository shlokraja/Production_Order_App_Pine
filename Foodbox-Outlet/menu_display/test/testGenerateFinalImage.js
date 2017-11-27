var generateFinalImage = require('../generateFinalImage');

describe('generate image tests', function() {
  it('basic test', function(done){

    // generateFinalImage(4, [1,2,3,4,5,6,7,8,9,10]);

    // generateFinalImage(4, [1,2,3,4,5,6,7,8]);

    // generateFinalImage(4, [1,2,3,4,5]);

    // generateFinalImage(2, [1,2,3,4,5,6,7,8,9,10]);

    // generateFinalImage(3, [1,2,3,4,5,6,7,8]);

    // generateFinalImage(3, [1,2]);

    var ll = []
    for (var i=0; i<33; i++) {
        ll.push(2209);
    }
    generateFinalImage(7, ll);

    //done();
  });
});
