var isOngo_Auth_Performed = false;
var isSingapore_Auth_Performed = false;
var isMswipe_Auth_Performed = false;
var isPineLabs_Auth_Performed = false;
$(document).ready(function () {
    Android.getPaymentGatewayTypes();
    $('#MSwipe_Payment_Details').hide();
    $('#Ongo_Payment_Details').hide();
    $('#Singapore_Payment_Details').hide();
    $('#PineLabs_Payment_Details').hide();
    $('#payment_type-button').children('span').hide();
    $('#country_type-button').children('span').hide();
    var selectedType = $("#payment_type option:selected").text();
    if (selectedType == 'SingaporePayment') {
        $('#Singapore_Payment_Details').show();
        $('#MSwipe_Payment_Details').hide();
        $('#Ongo_Payment_Details').hide();
        $('#PineLabs_Payment_Details').hide();
    }
});

function setPaymentValues(paymentTypes) {
    var values = JSON.parse(paymentTypes);
    var sorted_arr = values.slice().sort();
    var option = '';
    var results;

    for (var i = 0; i < sorted_arr.length; i++) {
        if (sorted_arr[i + 1] == sorted_arr[i]) {
            results = sorted_arr[i];
        } else {
            option += '<option value="' + values[i] + '">' + values[i] + '</option>';
        }
    }
    // $('#payment_type').append(option);

set_Payment_Type();
    /*var results = $("#payment_type option:selected").text();
    alert(results);
    if (results == 'Ongo') {
        $('#Ongo_Payment_Details').show();
        $('#MSwipe_Payment_Details').hide();
        $('#Singapore_Payment_Details').hide();
        $('#PineLabs_Payment_Details').hide();
    } else if (results == 'MSwipeInterface') {
        $('#MSwipe_Payment_Details').show();
        $('#Ongo_Payment_Details').hide();
        $('#Singapore_Payment_Details').hide();
        $('#PineLabs_Payment_Details').hide();
    }
    else if (results == 'SingaporePayment') {
        $('#Singapore_Payment_Details').show();
        $('#MSwipe_Payment_Details').hide();
        $('#Ongo_Payment_Details').hide();
        $('#PineLabs_Payment_Details').hide();
    }
    else if (results == 'PineLabsPayment') {
        $('#Singapore_Payment_Details').hide();
        $('#MSwipe_Payment_Details').hide();
        $('#Ongo_Payment_Details').hide();
        $('#PineLabs_Payment_Details').show();
    }
    else {
        $('#MSwipe_Payment_Details').hide();
        $('#Ongo_Payment_Details').hide();
        $('#Singapore_Payment_Details').hide();
        $('#PineLabs_Payment_Details').hide();
    }*/

    //$("option:selected").prop("selected", false)
    $('option:selected', 'select[name="options"]').removeAttr('selected');
    $('select[name="options"]').find('option:contains(' + results + ')').attr("selected", true);
}


$('#country_type').change(function () {

    var selected_country_type = $("#country_type option:selected").text();
    $('#country_type-button').children('span').hide();
});

$('#payment_type').change(function () {

set_Payment_Type();
  $('#payment_type-button').children('span').hide();
});

//new function to set payment type
function set_Payment_Type()
{
var selectedType = $("#payment_type option:selected").text();

    if (selectedType == 'Ongo') {
        $('#Ongo_Payment_Details').show();
        $('#MSwipe_Payment_Details').hide();
        $('#Singapore_Payment_Details').hide();
        $('#PineLabs_Payment_Details').hide();
    } else if (selectedType == 'MSwipeInterface') {
        Android.setSelectedPaymentGateway(selectedType);
        $('#MSwipe_Payment_Details').show();
        $('#Ongo_Payment_Details').hide();
        $('#Singapore_Payment_Details').hide();
        $('#PineLabs_Payment_Details').hide();
    }
    else if (selectedType == 'SingaporePayment') {
        $('#Singapore_Payment_Details').show();
        $('#MSwipe_Payment_Details').hide();
        $('#Ongo_Payment_Details').hide();
        $('#PineLabs_Payment_Details').hide();

    }
    else if (selectedType == 'PineLabsPayment') {

         $('#PineLabs_Payment_Details').show();
        $('#Singapore_Payment_Details').hide();
        $('#MSwipe_Payment_Details').hide();
        $('#Ongo_Payment_Details').hide();

    }
    else {
        $('#MSwipe_Payment_Details').hide();
        $('#Ongo_Payment_Details').hide();
        $('#Singapore_Payment_Details').hide();
        $('#PineLabs_Payment_Details').hide();
    }
    }

$('#authenticate_ongo_device_btn').click(function () {
    isOngo_Auth_Performed = true;
    var selectedType = $("#payment_type option:selected").text();
    var mer_id = $.trim($('#Ongo_Merchant_ID').val());
    var ter_id = $.trim($('#Ongo_Terminal_ID').val());
    var blu_name = $.trim($('#Ongo_Blutooth_Name').val());
    var blu_adrs = $.trim($('#Ongo_Blutooth_Addrs').val());

    Android.initiateOngoAuthentication(selectedType
    , blu_name
    , blu_adrs
    , ter_id
    , mer_id);
    alert("Device Authenticated Successfully");
});

$("#authenticate_Singapore_device_btn").click(function () {
    isSingapore_Auth_Performed = true;
    var selectedType = $("#payment_type option:selected").text();
    var ip_Address = $.trim($('#Singapore_IP_Address').val());
    var port_number = $.trim($('#Singapore_Port_Number').val());
    Android.initiateSingporeAuthentication(selectedType, ip_Address, port_number);
    alert("Device Authenticated Successfully");
});

$("#authenticate_PineLabs_device_btn").click(function () {
    isPineLabs_Auth_Performed = true;
    var selectedType = $("#payment_type option:selected").text();
    var merchant_ID = $.trim($('#PineLabs_Merchant_ID').val());
    var security_Token = $.trim($('#PineLabs_Security_Token').val());
    var store_Pos_Code = $.trim($('#PineLabs_Store_Pos_Code').val());
    var IMEI = $.trim($('#PineLabs_IMEI').val());
    var User_ID = $.trim($('#PineLabs_User_ID').val());
    var transaction_Number = $.trim($('#PineLabs_TransactionNumber').val());
    Android.initiatePineLabsAuthentication(selectedType, merchant_ID, security_Token,store_Pos_Code,IMEI,User_ID,transaction_Number);
    alert("Device Authenticated Successfully");
});

// button toggle for menu sections
$("#selector_button").click(function () {
    if (in_dispenser) {
        $("#dispenser_items").hide();
        $("#outside_items").show();
        $("#button_text").text("MEALS");
        $("#meals_img").attr("src", "img/meals.png");
        in_dispenser = false;
    } else {
        $("#outside_items").hide();
        $("#dispenser_items").show();
        $("#button_text").text("SNACKS & DRINKS");
        $("#meals_img").attr("src", "img/teacup.png");
        in_dispenser = true;
    }
});

$("#increase").bind("click", onIncreaseClick);

// updating the item quantity
function onIncreaseClick() {
    var current_val = parseInt($("#quantity").text());
    var item_code = parseInt($($(this).parents()[2]).children("#item_code").val());
    var new_val = current_val + 1;
    // check if item is in dispenser
    if (isItemInDispenser(item_code)) {
        if (new_val <= original_quantity) {
            $("#quantity").text(new_val);
            $("#main_alert_text").css("visibility", "hidden");
        } else {
            $(this).unbind("click");
            $("#decrease").unbind("click");
            // if yes, try to lock and then see
            tryLockItem(item_code, function (response) {
                $("#increase").bind("click", onIncreaseClick);
                $("#decrease").bind("click", onDecreaseClick);
                if (response.error) {
                    $("#main_alert_text #main_alert_sub").text('Outlet connectivity issues');
                    return;
                }

                if (response.available) {
                    $("#quantity").text(new_val);
                    $("#main_alert_text").css("visibility", "hidden");
                } else {
                    $("#main_alert_text #main_alert_sub").text('Only ' + current_val + ' items present.');
                    $("#main_alert_text").css("visibility", "visible");
                    $("#quantity").text(current_val);
                }
            });
        }
    } else {
        // if no , then increase no.
        $("#quantity").text(new_val);
        $("#main_alert_text").css("visibility", "hidden");
    }
}

function isItemInDispenser(item_code) {
    if (isTestModeItem(item_code) && TEST_MODE) {
        return true;
    }
    if (price_data[item_code]["location"] == "dispenser") {
        return true;
    } else {
        return false;
    }
}

$("#decrease").bind("click", onDecreaseClick);

function onDecreaseClick() {
    var current_val = parseInt($("#quantity").text());
    if (current_val <= 1) {
        // Alerting with the user
        $("#main_alert_text #main_alert_sub").text('This will remove the item(if exists) from order.');
        $("#main_alert_text").css("visibility", "visible");
    } else {
        $("#main_alert_text").css("visibility", "hidden");
    }
    if (current_val > 0 && (current_val - 1 >= original_quantity)) {
        var item_code = parseInt($($(this).parents()[2]).children("#item_code").val());
        if (isItemInDispenser(item_code)) {
            decreaseLockByOne(item_code);
        }
    }
    var new_val = current_val < 1 ? current_val : current_val - 1;
    $("#quantity").text(new_val);
}

$("#coke_increase").click(function () {
    var current_val = parseInt($("#coke_quantity").text());
    var new_val = current_val + 1;
    var item_quantity = parseInt($("#quantity").text());
    if (new_val > item_quantity) {
        $("#alert_text #alert_sub").text('Cannot order more Cokes than food');
        $("#alert_text").css("visibility", "visible");
        new_val = item_quantity;
    } else {
        $("#alert_text").css("visibility", "hidden");
    }
    $("#coke_quantity").text(new_val);
});
$("#coke_decrease").click(function () {
    var current_val = parseInt($("#coke_quantity").text());
    var new_val = current_val < 1 ? current_val : current_val - 1;
    $("#coke_quantity").text(new_val);
    $("#alert_text").css("visibility", "hidden");
});

$("#pop_cancel_btn").click(function () {
    // check if the item is in current order
    var parentDiv = $(this).parents()[3];
    var item_code = parseInt($(parentDiv).find("#item_code").first().val());
    var quantity = parseInt($(parentDiv).find("#quantity").first().text());
    // yes -> unlock the diff
    // no -> unlock the num that is there
    if (current_order.hasOwnProperty(item_code)) {
        var diff_quantity = quantity - original_quantity;
        if (diff_quantity > 0) {
            decreaseLockByQty(item_code, diff_quantity);
        }
    } else {
        // A bit of complicated logic.
        if (original_quantity != 0) {
            if (quantity == 0) {
                decreaseLockByOne(item_code);
            } else {
                decreaseLockByQty(item_code, quantity);
            }
        } else {
            decreaseLockByQty(item_code, quantity);
        }
    }
    $('div').filter('[data-role="page"]').removeClass("grayscale");
});

$("#mobile_num").on('input', function () {
    if (mobileNumberValidation($("#mobile_num").val())) {
        $("#mobile_num").trigger("blur");
    }
});

$("#settings_cancel_btn").click(function () {
    location.reload();
});

// Get the pop values and update the order summary
$("#pop_action_btn").click(function () {
    $('div').filter('[data-role="page"]').removeClass("grayscale");
    var parentDiv = $(this).parents()[3];
    var item_code = parseInt($(parentDiv).find("#item_code").first().val());
    var quantity = parseInt($(parentDiv).find("#quantity").first().text());
    var coke_quantity = parseInt($(parentDiv).find("#coke_quantity").first().text());

    // Checking if  new qty is less than original, then unlock the diff
    if (quantity < original_quantity) {
        var diff_quantity = original_quantity - quantity;
        decreaseLockByQty(item_code, diff_quantity);
    }

    // Adding it to current order only if it the main item is >0
    if (quantity) {
        // checking whether stock is available or not
        updateLock(item_code, quantity, coke_quantity);
    } else {
        removeLock(item_code);
    }
    updateOrderSummary();
});

$("#order_summary h3").taphold(function () {
    console.log('taphold triggered');
    $('#passwordPopUp').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
});

$(document).on('closed.fndtn.reveal', '#passwordPopUp', function () {
    if ($("#passwd").val() == SETTINGS_PASSWORD) {
        $("#settings_passwd").val(SETTINGS_PASSWORD);
        $("#hq_url").val(HQ_URL);
        $("#outlet_url").val(OUTLET_URL);
        $("#websocket_url").val(WEBSOCKET_URL);
        $("#outlet_id").val(OUTLET_ID);
        $("#counter_code").val(COUNTER_CODE);
        $("#accept_cards").prop("checked", ACCEPT_CREDIT_CARDS);
        $("#accept_cash").prop("checked", ACCEPT_CASH);
        $("#show_snacks").prop("checked", SHOW_SNACKS);
        $("#mobile_mandatory").prop("checked", MOBILE_MANDATORY);
        $("#item_images").prop("checked", item_images);
        $("#others_mandatory").prop("checked", OTHERS_MANDATORY);
        $("#country_type").val(COUNTRY_TYPE);
        //alert(simpleStorage.get("PAYMENT_TYPE"));
        $("#payment_type").val(simpleStorage.get("PAYMENT_TYPE"));
        set_Payment_Type();
        Android.initiateLogFileRead();
        $("#mswipe_username").val(MSWIPE_USERNAME);
        $("#mswipe_password").val(MSWIPE_PASSWORD);

        $('#Ongo_Merchant_ID').val(simpleStorage.get("MERCHANT_ID"));
        $('#Ongo_Terminal_ID').val(simpleStorage.get("TERMINAL_ID"));
        $('#Ongo_Blutooth_Name').val(simpleStorage.get("BLUETOOTH_NAME"));
        $('#Ongo_Blutooth_Addrs').val(simpleStorage.get("BLUETOOTH_ADDRESS"));

        $('#Singapore_IP_Address').val(simpleStorage.get("IP_ADDRESS"));
        $('#Singapore_Port_Number').val(simpleStorage.get("PORT_NUMBER"));


       // $('#PineLabs_IMEI').val(simpleStorage.get("IMEI"));

        $('#PineLabs_Merchant_ID').val(simpleStorage.get("PINE_MERCHANT_ID"));
        $('#PineLabs_Security_Token').val(simpleStorage.get("SECURITY_TOKEN"));
        $('#PineLabs_Store_Pos_Code').val(simpleStorage.get("STORE_POS_CODE"));
        $('#PineLabs_IMEI').val(simpleStorage.get("IMEI"));
        $('#PineLabs_User_ID').val(simpleStorage.get("USER_ID"));
        $('#PineLabs_TransactionNumber').val(simpleStorage.get("TRANSACTION_NUMBER"));
        $('#PineLabs_User_ID').val(simpleStorage.get("USER_ID"));


        $("#inspire_digest_auth").val(INSPIRENETZ_DIGEST_AUTH);
        $("#inspire_http_url").val(INSPIRENETZ_HTTP_URL);
        $("#inspire_username").val(INSPIRENETZ_USERNAME);
        $("#inspire_password").val(INSPIRENETZ_PASSWORD);
        $('#settingsModal').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
    } else {
        alert('Incorrect password');
    }
    $("#passwd").val("");
});

$("#saveSettings_btn").click(function () {
    var selectedType = $("#payment_type option:selected").text();
    var auth = true;
    if (selectedType == "Ongo" && !isOngo_Auth_Performed) {
        auth = false;
    }
    else if (selectedType == "SingaporePayment" && !isSingapore_Auth_Performed) {
        auth = false;
    }
    else if (selectedType == "MSwipeInterface" && !isMswipe_Auth_Performed) {
        auth = false;
    }
    else if (selectedType == "PineLabsPayment" && !isPineLabs_Auth_Performed) {
        auth = false;
    }
    if (!auth) {
        alert("Please Authenticate device before use.")
        return;
    }
    saveSettings();
    // Reloading the page, now that settings have changed
    location.reload();
});

$("#connect_device_btn").click(function () {
    saveSettings();
    Android.initiateConnection();
});

$("#authenticate_device_btn").click(function () {
    saveSettings();
    Android.initiateAuthentication(MSWIPE_USERNAME, MSWIPE_PASSWORD);
    isMswipe_Auth_Performed = true;
    alert("Device Authenticated Successfully");
});

$("#bank_summary_btn").click(function () {
    saveSettings();
    Android.initiateShowBankSummary();
});

$("#email_logs_btn").click(function () {
    Android.mailLogs();
});

$("#clear_locks_btn").click(function () {
    clearAllLocks();
});

$("#cancel_order, #cancel_order2").click(function () {
    // wipe out current order
    removeOrderLock(current_order);
});

$("#editText_btn").click(function () {
    var text = $(this).text();
    if (text == "Edit") {
        $(this).text("Disable");
    } else {
        $(this).text("Edit");
    }
    // Depending on the state, enabling or disabling the textboxes
    $("#settingsModal input[type=text], #settingsModal input[type=password]").each(function () {
        if ($(this).prop("disabled")) {
            $(this).prop("disabled", false);
        } else {
            $(this).prop("disabled", true);
        }
    });
});


$("#proceed_order").click(function () {
    var tableDiv = $("#finalOrder table tbody");
     $('#card_checkout .swipe_card_btn_div .back_btn').text('Back');
    $(tableDiv).empty();
    if (!Object.keys(current_order).length) {
        alert("Please select an item");
        return false;
    }
    var gst_percent = 0;
    var total_gst = 0;
    var total_amount = 0;
    var total_st_amount = 0;
    var total_vat_amount = 0;
    var total_bill_amount = 0;
    var decimal_Count = 0;
    if (simpleStorage.get("COUNTRY_TYPE") != 'India') {
        decimal_Count = 2;
    }

    for (var key in current_order) {
        var quantity = current_order[key][0];
        var coke_quantity = current_order[key][1];

        // If this is a test mode item, then don't do anything
        if (isTestModeItem(key)) {
            $(tableDiv).append('<tr><td class="d_item">' + key + '</td><td>' + quantity + '</td><td class="right_justified"><div class="rupee">' + CURRENCY_SYM + ' </div>1</td></tr>');
            total_amount += 1;
            total_gst += 1;
            gst_percent += 1;
            total_vat_amount += 1;
            total_st_amount += 1;
            total_bill_amount += 1;
            continue;
        }
        gst_percent = price_data[key]["cgst_percent"] + price_data[key]["sgst_percent"];

        //var mrp_without_tax = (price_data[key]["mrp"]*100)/(100+(price_data[key]["service_tax_percent"]* price_data[key]["abatement_percent"]/100)+price_data[key]["vat_percent"]);
        var mrp_without_tax = (price_data[key]["mrp"] * 100) / (100 + price_data[key]["cgst_percent"] + price_data[key]["sgst_percent"]);

        var price = mrp_without_tax * quantity;
        total_amount += price;
        if (current_order[key][2] === "dispenser") {
            $(tableDiv).append('<tr><td class="d_item">' + price_data[key]["name"] + '</td><td>' + quantity + '</td><td class="right_justified"><div class="rupee">' + CURRENCY_SYM + ' </div>' + price.toFixed(decimal_Count) + '</td></tr>');
        } else {
            $(tableDiv).append('<tr><td>[' + price_data[key]["name"] + ']</td><td>' + quantity + '</td><td class="right_justified"><div class="rupee">' + CURRENCY_SYM + ' </div>' + price.toFixed(decimal_Count) + '</td></tr>');
        }

        if (coke_quantity) {
            //var coke_mrp = price_data[key]["coke_details"]["mrp"] * price_data[key]["coke_details"]["discount_percent"] / 100;
            var coke_mrp = price_data[key]["coke_details"]["mrp"] - (price_data[key]["coke_details"]["mrp"] * price_data[key]["coke_details"]["discount_percent"] / 100);

            //var c_mrp_without_tax = (coke_mrp * 100)/(100+(price_data[key]["coke_details"]["st"]*price_data[key]["coke_details"]["abt"])+price_data[key]["coke_details"]["vat"]);
            var c_mrp_without_tax = (coke_mrp * 100) / (100 + price_data[key]["coke_details"]["cgst_percent"] + price_data[key]["coke_details"]["sgst_percent"]);

            var coke_price = c_mrp_without_tax * coke_quantity;
            $(tableDiv).append('<tr onclick=showPopup(' + key + ');><td>+coke</td><td>' + coke_quantity + '</td><td class="right_justified"><div class="rupee">' + CURRENCY_SYM + ' </div>' + coke_price.toFixed(decimal_Count) + '</td></tr>');
            total_amount += coke_price;

            total_gst += c_mrp_without_tax * coke_quantity * gst_percent / 100;
            //total_st_amount += c_mrp_without_tax * coke_quantity * (price_data[key]["coke_details"]["st"] * price_data[key]["coke_details"]["abt"]) / 100;
            //total_vat_amount += c_mrp_without_tax * coke_quantity * price_data[key]["coke_details"]["vat"] / 100;
        }
        total_bill_amount += price_data[key]["mrp"] * quantity + price_data[key]["coke_details"]["mrp"] - (price_data[key]["coke_details"]["mrp"] * price_data[key]["coke_details"]["discount_percent"] / 100) * coke_quantity;

        //total_bill_amount += price_data[key]["mrp"] * quantity + (price_data[key]["coke_details"]["mrp"] * price_data[key]["coke_details"]["discount_percent"] / 100) * coke_quantity;
        total_gst += mrp_without_tax * quantity * gst_percent / 100;
        //total_st_amount += mrp_without_tax * quantity * (price_data[key]["service_tax_percent"] * price_data[key]["abatement_percent"])  / 10000;
        //total_vat_amount += mrp_without_tax * quantity * price_data[key]["vat_percent"] / 100;
    }

    $("#total_without_tax .num").text(total_amount.toFixed(decimal_Count));
    $("#total_gst .num").text(total_gst.toFixed(decimal_Count));
    //$("#total_sales_tax .num").text(total_st_amount.toFixed(decimal_Count));
    $("#total .num").text(total_bill_amount.toFixed(decimal_Count));
    total_money = total_bill_amount.toFixed(decimal_Count);
    // Enabling the cash/card buttons otherwise they get disabled from foundation
    $("#cash_btn").prop("disabled", false);
    $("#card_btn").prop("disabled", false);
    if (ACCEPT_CREDIT_CARDS) {
        $("#card_btn").show();
    } else {
        $("#card_btn").hide();
    }

    if (ACCEPT_CASH) {
        $("#cash_btn").show();
    } else {
        $("#cash_btn").hide();
    }
    if (OTHERS_MANDATORY) {
        $("#other_btn").show();
    } else {
        $("#other_btn").hide();
    }

    if (ACCEPT_CREDIT_CARDS && ACCEPT_CASH && OTHERS_MANDATORY) {
        $("#checkout_buttons").css("width", "20% !important");
    }
    else if (ACCEPT_CREDIT_CARDS && ACCEPT_CASH) {
        $("#checkout_buttons").css("width", "81% !important");
    } else if (ACCEPT_CREDIT_CARDS || ACCEPT_CASH) {
        $("#checkout_buttons").css("width", "55% !important");
    }

    // populate the sides text depending on whether sides are there or not
    populateSides();

    $("#mobile_num").val("");
    $('#finalOrder').foundation('reveal', 'open',
          { close_on_background_click: false, animation: 'none' });
    // Creating the connection at this step to save time - only if cards enabled
    if (ACCEPT_CREDIT_CARDS) {
        Android.initiateConnection();
    }
    $('div').filter('[data-role="page"]').addClass("grayscale");
});

$("#back_btn").click(function () {
    $("#mobile_num").val("");
    $('div').filter('[data-role="page"]').removeClass("grayscale");
    // Disconnect here from WisePad (if Cards enabled)
    if (ACCEPT_CREDIT_CARDS) {
        Android.initiateDisconnection();
    }
});

$("#card_checkout button").click(function () {
    // Cancelling the checkCard() in the wisepad device
    Android.initiateCancelCheckCard();
    // Closing the card checkout button and going back to the checkout screen
    $('#card_checkout').foundation('reveal', 'close');
    $('#finalOrder').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
});

$("#card_failure button").click(function () {
    Android.initiateConnection();
    $("#card_failure .title").text("SWIPE CARD");
    // Closing the card checkout button and going back to the checkout screen
    $('#card_failure').foundation('reveal', 'close');
    $('#finalOrder').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
});

$("#other_btn").click(function () {
    var mobile_num = '';
    if (MOBILE_MANDATORY) {
        mobile_num = $("#mobile_num").val();
        if (!mobile_num || !mobileNumberValidation(mobile_num)) {
            // if no mobile number is entered, show the error and return
            if (mobile_num == "") {
                alert("Please enter the Mobile Number");
            }
            else {
                alert("Mobile Number is incorrect");
            }

            $("#cash_btn").prop("disabled", false);
            return false;
        }
    }
    $('#othersPayment').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
});

$("#payment_type_back_btn").click(function () {
    //alert("back");
    //Android.initiateConnection();
    // $("#card_failure .title").text("SWIPE CARD");
    // Closing the card checkout button and going back to the checkout screen
    //$('#card_failure').foundation('reveal','close');
    $('#othersPayment').foundation('reveal', 'close');
    $('#finalOrder').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
});

$("#payment_type_others_ok").click(function () {
    var selectedType = $("#others_payment_type option:selected").val();
    if (selectedType != '--Select Payment Type--') {
        if (simpleStorage.get("TEST_MODE") == false) {
            submitpayment(selectedType);
        }
        else {
            if (confirm("Application is running in Test Mode. Click Ok to proceed.") == true) {
                submitpayment(selectedType);
            }
        }
    }
    else {
        alert("Select payment type");
    }

});
$("#cash_btn").click(function () {
    if (simpleStorage.get("TEST_MODE") == false) {
        submitpayment("cash");
    }
    else {
        if (confirm("Application is running in Test Mode. Click Ok to proceed.") == true) {
            submitpayment("cash");
        }
    }
});

function submitpayment(payment_mode) {
    var mobile_num = '';
    mode_payment = payment_mode;

    //alert(mode_payment);
    if (MOBILE_MANDATORY) {
        mobile_num = $("#mobile_num").val();
        if (!mobile_num || !mobileNumberValidation(mobile_num)) {
            // if no mobile number is entered, show the error and return
            if (mobile_num == "") {
                alert("Please enter the Mobile Number");
            }
            else {
                alert("Mobile Number is incorrect");
            }

            $("#cash_btn").prop("disabled", false);
            return false;
        }
    }
    savings = 0;
    // placing the order to LC
    pushOrder(mode_payment, 0, mobile_num, "", "");
    //$("#cash_checkout .savings_text").css("visibility", "hidden");
    // showing the dialog finally, after updating the total_money amount
    // P.S. - If there is no internet, it will just go with the original money
    $("#card_success .cash_amount").text(total_money);
    $("#cash_checkout .cash_amount").text(total_money);
    $("#card_failure .cash_amount").text(total_money);
    $('#finalOrder').foundation('reveal', 'close');
    // disconnecting from mswipe device as this was a cash transaction
    // (only if cards enabled)
    if (ACCEPT_CREDIT_CARDS) {
        Android.initiateDisconnection();
    }
}

$("#card_btn").click(function () {
    if (simpleStorage.get("TEST_MODE") == false) {
        proceedCardPayment();
    }
    else {
        if (confirm("Application is running in Test Mode. Click Ok to proceed.") == true) {
            proceedCardPayment();
        }
    }
});

function proceedCardPayment() {
    var selectedType = $("#payment_type option:selected").text();
    if (selectedType == '--Select--') {
        alert("Please select Payment Gateway.");
        return;
    }
    var mobile_num = '';
    if (MOBILE_MANDATORY) {
        mobile_num = $("#mobile_num").val();
        if (!mobile_num || !mobileNumberValidation(mobile_num)) {
            // if no mobile number is entered, show the error and return
            if (mobile_num == "") {
                alert("Please enter the Mobile Number");
            }
            else {
                alert("Mobile Number is incorrect");
            }
            return false;
        }
    }
    savings = 0;
    // saving the order details so that they can be retrieved and pushed later
    simpleStorage.set("order", {
        "mode": "card",
        "savings": 0,
        "mobile_num": mobile_num
    });
    $("#card_checkout .savings_text").css("visibility", "hidden");

    // showing the dialog finally, after updating the total_money amount
    // P.S. - If there is no internet, it will just go with the original money
    $("#card_success .cash_amount").text(total_money);
    $("#card_failure .cash_amount").text(total_money);
    $('#finalOrder').foundation('reveal', 'close');
    $('#card_checkout').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
    $('div').filter('[data-role="page"]').addClass("grayscale");
    // starting to play the video
    var video = document.getElementById("swipe_video");
    video.loop = false;
    video.addEventListener('ended', function () {
        video.load();
    }, false);
    video.play();
    // Calling the android function to check for card swipe
    //alert("Total money:"+total_money.toString()+" test:"+TEST_MODE);
    Android.initiateCheckCard(total_money.toString(), TEST_MODE , mobile_num );
}

