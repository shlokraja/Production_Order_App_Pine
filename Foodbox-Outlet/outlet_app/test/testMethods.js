var pg = require('pg');
var express = require('express');
var requestretry = require('requestretry');
//var config = require('../models/config');
var general = require('../api/general');
var nodemailer = require('nodemailer');

//// // For test configuration
//var conString = "postgres://atchayam:foodbox123@localhost/foodbox19092016";
//var firebase_connection = "https://owltech-hq.firebaseio.com";
//var firebase_connection_outlet = "https://owltech-outlet.firebaseio.com";

//server_ip_address = "192.168.1.24";
//server_port = '5501';
var router = express();
var listen_port = '5500';

router.listen(listen_port, function () {
    general.genericError('Example router listening on port ' + listen_port + '!');
});


//var client = new pg.Client(conString);
//client.connect();

router.get('/testmail', function (req, res, next)
{
    try
    {
        console.log("Test mail called");

        var transporter_mail = nodemailer.createTransport({
            host: "smtp.gmail.com", // hostname
            port: 465,
            secure: true,
            auth: {
                user: 'no-reply@atchayam.in',
                pass: 'Atchayam123'
            }
        }, {
            // default values for sendMail method
            from: 'no-reply@atchayam.in',
            headers: {
                'My-Awesome-Header': '123'
            }
        });

        //;shlokgunaseelan@gmail.com
        var mail = {
            from: 'no-reply@atchayam.in', // sender address
            to: "gunaseelan.r@shloklabs.com", // list of receivers
            subject: 'Pending Reconcile Items in SPI on ', // Subject line
            text: "Test content",
            html: "Test content"
        }
        console.log("mail " + mail);
        transporter_mail.sendMail(mail, function (error, response) {
            if (error)
            {
                console.log(error);
            } else
            {
                console.log("message sent: " + response.message);
            }

        });

        res.send('success');
    } catch (e)
    {
        console.log("Error: " + e.message);
    }

});

module.exports = router;