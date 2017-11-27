var fs = require('fs');
var f = 'log.txt';

function debug(message) {
    var dateObj = new Date();
    var date = dateObj.toDateString();
    var time = dateObj.toLocaleTimeString();

    message = message + " Date: " + date + " " + time;
    fs.appendFile(f, message + '\n', function (err) {
        if (err)
            console.error(err);
        console.log(message + '\n');
    });
}

debug("Test message with date");


function handleBillDispense(data) {
    debug("handleBillDispense started");
    // document.getElementById("idtest").innerHTML = "Test collect cash";
    var date_obj = new Date();
    var tag = data["tag"];
    // Go through all the previous keys to see if a tag is already
    // present or not
    var isAlreadyPresent = false;
    simpleStorage.index().map(function (key) {
        if (key == "bill_" + tag)
        {
            isAlreadyPresent = true;
        }
    });
    if (isAlreadyPresent)
    {
        console.log("not doing anything as this is a duplicate");
        return;
    }
    var div_id = "bill_" + tag;
    // Storing the data for the pop up and later bill printing
    simpleStorage.set(div_id, data);

    if ($("#incoming-po-dialog").length == 0)
    {
        // Returning if this is not the home page
        return;
    }
    showBillDispenseInDOM(data, div_id);
}

function showBillDispenseInDOM(data, div_id) {
    debug("showBillDispenseInDOM" + data);
    var counter_code = data["counter_code"];
    var order_details = data["order_details"];
    var bill_no = data["bill_no"];
    var sides = data["sides"];
    var total_amount = 0;
    for (var item_id in order_details)
    {
        total_amount += order_details[item_id]["price"];
    }
    for (var item_id in sides)
    {
        total_amount += sides[item_id]["price"];
    }

    var rem = total_amount % 1000;
    var quot1k = parseInt(total_amount / 1000);
    var IN1 = (quot1k + 1) * 1000;
    $("#left_pane > .cash_change tbody .change_1000").text('Change for ' + IN1 + ' =' + (IN1 - total_amount));

    if (rem < 500)
    {
        var IN2 = (quot1k * 1000) + 500;
        $("#left_pane > .cash_change tbody .change_500").text('Change for ' + IN2 + ' =' + (IN2 - total_amount));
    } else
    {
        $("#left_pane > .cash_change tbody .change_500").remove();
    }

    var quot100 = parseInt(rem / 100);
    if (quot100 != 4 && quot100 != 9)
    {
        var IN3 = (quot1k * 1000) + ((quot100 + 1) * 100);
        $("#left_pane > .cash_change tbody .change_100").text('Change for ' + IN3 + ' =' + (IN3 - total_amount));
    } else
    {
        $("#left_pane > .cash_change tbody .change_100").remove();
    }

    $("#collect_cash").append('<div class="cash_notification">\
     Bill #'+ bill_no + ' collect INR ' + total_amount + ' from counter ' + counter_code + '  \
    <a id="'+ div_id + '" href="javascript:void(0)" class="done btn btn-default btn-raised"> \
    <img src="img/icons/Delivered.png" /><span>Done</span></a></div>').append($("#left_pane > .cash_change").clone());
}

module.exports = { handleBillDispense: handleBillDispense };