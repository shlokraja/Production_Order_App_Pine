var debug = require('debug')('menu_display');
var format = require('string-format');
var gm = require('gm');
var fs = require('fs');
var fse = require('fs-extra');
var md5File = require('md5-file');

// a b c d  ->  ab
//              cd
// getting the source and target folders
var source_folder = process.env.SOURCE_FOLDER;
var target_folder = process.env.TARGET_FOLDER;
var usebigtv = process.env.USE_BIGTV;

function generateCoalescedImage(num_live_screens) {
    if (usebigtv == undefined){
    	return;
    }
    // Do we need to do an update?
    if (!global.updatebigtv){
	    debug("bigtv:: Skipping coalesced image");

        // No - done
        return;
    }

    // OK, updating it, toggle need for update
    global.updatebigtv = false;

    setTimeout(function(){    
     debug("bigtv:: Writing coalesced image");    

     // First check if the images have changed
     // If any of them has, then run the code
     gm()
        .in('-page', '+0+0')  // Custom place for each of the images
        .in(target_folder + '/final0.png')
        .in('-page', '+1280+0')
        .in(target_folder + '/final1.png')
        .in('-page', '+0+720')
        .in(target_folder + '/final2.png')
        .in('-page', '+1280+720')
        .in(target_folder + '/final3.png')
        .mosaic()  // Merges the images as a matrix
        .write(target_folder + '/finalproj_tmp.png', function (err) {
            if (err) {
            	// Redo bigtv
            	global.updatebigtv = true;
                return debug(err);
            }

            fse.copySync(target_folder + '/finalproj_tmp.png',
              target_folder + '/finalproj.png');
            debug("Written coalesced image");
        });
    }, 1500);
}

module.exports = generateCoalescedImage;
