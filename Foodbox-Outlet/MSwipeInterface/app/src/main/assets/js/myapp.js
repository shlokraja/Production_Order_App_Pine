// global variables accessible from anywhere in the app
// dict tracking price data and other details for the menu
var price_data = {};
// dict tracking current order details
var current_order = {};
// variable to store the original quantity when opening the popup
var original_quantity = 0;
// flag tracking which section of the menu is open
var in_dispenser = true;
var MAX_MEAL_ITEMS = 24;
var MAX_SNACKS_ITEMS = 20;
var total_money = 0;
var RUN_COUNT = -1;
var CURRENCY_SYM; //indian Currency Entity (decimal) &#8377
var mobileNumberLength=0;
var socket = io.connect(WEBSOCKET_URL);

 if (COUNTRY_TYPE=="India") {
     mobileNumberLength=10;
     CURRENCY_SYM="₹";
    }else
    {mobileNumberLength=8;
     CURRENCY_SYM="$";
    }

$('.rupee').text(CURRENCY_SYM);

socket.on('reconnect', function (data)
{
    console.log("Reconnected to outlet.");
    $.ajax({
        url: OUTLET_URL + '/order_app/run_count/',
        dataType: 'json',
        timeout: 3000, //3 second timeout
        success: function (data)
        {
            if (data.run_count != RUN_COUNT)
            {
                // That means outlet has actually restarted
                // Unlocking all items which are there in the current order
                if (Object.keys(current_order).length != 0)
                {
                    removeOrderLock(current_order);
                    alert("System is restarting. Please order from scratch.");
                }
            }
        },
        error: function (jqxhr, textStatus, error)
        {
            var err_msg = textStatus + ", " + error;
            console.error("Getting run count failed: " + err_msg);
        }
    });

    getTestMode();
    getDispenserStatus();
    getStopOrdersState();
});

// Getting the test mode flag from LC
function getTestMode()
{
    $.ajax({
        url: OUTLET_URL + '/order_app/test_mode/',
        dataType: 'json',
        timeout: 3000, //3 second timeout
        success: function (data)
        {
            console.log("Received initial test mode flag as - ", data);
            TEST_MODE = data;
            simpleStorage.set("TEST_MODE", TEST_MODE);
        },
        error: function (jqxhr, textStatus, error)
        {
            var err_msg = textStatus + ", " + error;
            console.error("Request Failed: " + err_msg);
            // Checking the test mode flag locally
            if (simpleStorage.get("TEST_MODE"))
            {
                TEST_MODE = true;
            } else
            {
                TEST_MODE = false;
            }
        }
    });
}
// Getting the test mode flag from LC
function getDispenserStatus()
{
    $.ajax({
        url: OUTLET_URL + '/menu_display/dispenser_status/',
        dataType: 'text',
        timeout: 3000, //3 second timeout
        success: function (data)
        {
            console.log("Received dispenser status as - ", data);
            if (data == "working")
            {
                simpleStorage.set("order_delay", false);
                $("#order_delay_text").css("visibility", "hidden");
                if(SHOW_SNACKS) {
                    $("#selector_button").show();
                }
                else {
                    $("#selector_button").hide();
                }
            } else if (data == "loading")
            {
                simpleStorage.set("order_delay", true);
                $("#order_delay_text").css("visibility", "visible");
            }
        },
        error: function (jqxhr, textStatus, error)
        {
            var err_msg = textStatus + ", " + error;
            console.error("Request Failed: " + err_msg);
            simpleStorage.set("order_delay", false);
            $("#order_delay_text").css("visibility", "hidden");
                            if(SHOW_SNACKS) {
                                $("#selector_button").show();
                            }
                            else {
                                $("#selector_button").hide();
                            }
        }
    });
}

function getStopOrdersState()
{
    $.ajax({
        url: OUTLET_URL + '/order_app/stop_orders_state/',
        dataType: 'text',
        timeout: 3000, //3 second timeout
        success: function (data)
        {
            console.log("Received stop orders state as - ", data);
            simpleStorage.set("stop_orders", JSON.parse(data));
            if (JSON.parse(data))
            {
                // show the gray over lay
                $("#stop_orders").show();
            } else
            {
                // remove the gray over lay
                $("#stop_orders").hide();
            }
        },
        error: function (jqxhr, textStatus, error)
        {
            var err_msg = textStatus + ", " + error;
            console.error("Request Failed: " + err_msg);
            simpleStorage.set("stop_orders", false);
            $("#stop_orders").hide();
        }
    });
}

function getRunCount(mode)
{
    $.ajax({
        url: OUTLET_URL + '/order_app/run_count/',
        dataType: 'json',
        timeout: 3000, //3 second timeout
        success: function (data)
        {
            RUN_COUNT = data.run_count;
        },
        error: function (jqxhr, textStatus, error)
        {
            var err_msg = textStatus + ", " + error;
            console.error("Getting run count failed: " + err_msg);
        }
    });
}



// This will return the prices and the veg/non-veg flag
function getItemDetails()
{

  console.log('Begin ::Received price data');
    var jqxhr = $.getJSON(HQ_URL + '/food_item/price_info/' + OUTLET_ID)
    .done(function (data)
    {
        console.log('Received price data');
        for (var i = 0; i < data.length; i++)
        {
            price_data[data[i]["id"]] = {
                "mrp": data[i]["mrp"],
                "sgst_percent": data[i]["sgst_percent"],
                "cgst_percent": data[i]["cgst_percent"],
                "master_id": data[i]["master_id"],
                "name": data[i]["name"],
                "item_tag": data[i]["item_tag"],
                "veg": data[i]["veg"],
                "service_tax_percent": data[i]["service_tax_percent"],
                "abatement_percent": data[i]["abatement_percent"],
                "vat_percent": data[i]["vat_percent"],
                "location": data[i]["location"],
                "side_order": data[i]["side_order"],
                "restaurant_details": { "id": data[i]["r_id"],
                    "name": data[i]["r_name"],
                    "address": data[i]["r_address"],
                    "st_no": data[i]["r_st_no"],
                    "pan_no": data[i]["r_pan_no"],
                    "cgst_percent": data[i]["r_cgst_percent"],
                    "sgst_percent": data[i]["r_sgst_percent"],
                    "entity": data[i]["r_entity"],
                    "tin_no": data[i]["r_tin_no"]
                },
                "coke_details": { "id": data[i]["b_id"],
                    "name": data[i]["b_name"],
                    "mrp": data[i]["b_mrp"],
                    "st": data[i]["b_service_tax_percent"],
                    "abt": data[i]["b_abatement_percent"],
                    "vat": data[i]["b_vat_percent"],
                    "discount_percent": data[i]["discount_percent"],
                    "restaurant_details":
                                { "id": data[i]["b_r_id"],
                                    "name": data[i]["b_r_name"],
                                    "address": data[i]["b_r_address"],
                                    "st_no": data[i]["r_st_no"],
                                    "pan_no": data[i]["r_pan_no"],
                                    "tin_no": data[i]["b_r_tin_no"]
                                }
                },
                "heating_reqd": data[i]["heating_required"],
 		 "heating_reduction": data[i]["heating_reduction"],
         	 "condiment_slot": data[i]["condiment_slot"],
                "stock_quantity": -1
            }
        }
        populateSideItems();
        $.getJSON(OUTLET_URL + '/menu_display/stock_initial/')
      .done(function (data)
      {
          console.log("Received initial data ", data);
          handleStockCountData(data);
      })
      .fail(function (jqxhr, textStatus, error)
      {
          var err_msg = textStatus + ", " + error;
          console.error("Request Failed: " + err_msg);
      });
        // Setting up socket.io event handlers
        socket.on('stock_count', function (data)
        {
            console.log('Received stock data from socket.io- ' + JSON.stringify(data));
            handleStockCountData(data);
        });
    })
    .fail(function (jqxhr, textStatus, error)
    {
        var err_msg = textStatus + ", " + error;
        console.error("Request Failed: " + err_msg);
    });
}

socket.on('order_delay', function (delay_flag)
{
    console.log('Received order delay signal - ' + delay_flag);
    simpleStorage.set("order_delay", delay_flag);
    // If the dispenser is loading, show a delay msg at the top
    if (delay_flag)
    {
        $("#order_delay_text").css("visibility", "visible");
    } else
    {
        $("#order_delay_text").css("visibility", "hidden");
    }
});

socket.on('stop_orders', function (order_flag)
{
    console.log('Received order taking signal - ' + order_flag);
    // Setting the status of stop orders
    simpleStorage.set("stop_orders", order_flag);
    // If the dispenser is loading, show a delay msg at the top
    if (order_flag)
    {
        // show the gray over lay
        $("#stop_orders").show();
    } else
    {
        // remove the gray over lay
        $("#stop_orders").hide();
    }
});

if (typeof String.prototype.startsWith != 'function')
{
    // see below for better implementation!
    String.prototype.startsWith = function (str)
    {
        return this.indexOf(str) === 0;
    };
}

socket.on('dispenser_empty', function (empty_flag)
{
    console.log('Received dispenser empty signal - ' + empty_flag);
    // If the dispenser is empty, only show the outside items
    // and hide the selector button
    if (empty_flag)
    {
        $("#selector_button").hide();
        $("#outside_items").show();
        $("#dispenser_items").hide();
        $("#dispenser_items_TATA").hide();
        $("#button_text").text("MEALS");
        $("#meals_img").attr("src", "img/meals.png");
        in_dispenser = false;
    } else
    {
        if(SHOW_SNACKS) {
            $("#selector_button").show();
        }
        else {
            $("#selector_button").hide();
        }
        $("#outside_items").hide();
        if(!item_images) {
            $("#dispenser_items").show();
        }
        else {
            $("#dispenser_items_TATA").show();
        }
        $("#button_text").text("SNACKS & DRINKS");
        $("#meals_img").attr("src", "img/teacup.png");
        in_dispenser = true;
    }
});

socket.on('beverage_items', function (data)
{
    console.log("updating the beverage items");
    var outsideDiv = $("#outside_items");
    $(outsideDiv).empty();

    var counter = 0;
    data.map(function (item)
    {
        if (!item.visible)
        {
            simpleStorage.set(item.id + "_visibility", false);
            return;
        }
        simpleStorage.set(item.id + "_visibility", true);
        if (counter < MAX_SNACKS_ITEMS)
        {
            counter++;
            $(outsideDiv).append('<li onclick="showPopup(this);"><img class="side_picture" data-item-code="' + item.id + '" src="' + OUTLET_URL + '/order_app/image/' + item.master_id + '" /></li>');
        }
    });

    for (var i = counter; i < MAX_SNACKS_ITEMS; i++)
    {
        //$(outsideDiv).append('<li class="filler"><img class="side_picture filler_text" src="' + OUTLET_URL + '/order_app/image/" /></li>');
    }
});



socket.on('test_mode', function (flag)
{
    console.log('Received the test mode signal -' + flag);
    TEST_MODE = flag;
    simpleStorage.set("TEST_MODE", TEST_MODE);
    location.reload();
});


function populateSideItems()
{
    var counter = 0;
    var outsideDiv = $("#outside_items");
    if (!price_data)
    {
        return;
    }
    for (var item_id in price_data)
    {
        var veg = price_data[item_id]["veg"];
        var veg_class = veg ? "veg" : "non-veg";
        if (price_data[item_id]["location"] === "outside")
        {
            // check if the item should be shown in the UI
            if (shouldItemBeVisible(item_id) == false)
            {
                continue;
            }
            counter++;
            $(outsideDiv).append('<li onclick="showPopup(this);"><img class="side_picture" data-item-code="' + item_id + '" src="' + OUTLET_URL + '/order_app/image/' + price_data[item_id]["master_id"] + '" /></li>');
        }
    }
    if (!price_data.hasOwnProperty(item_id))
    {
        return;
    }
    for (var i = counter; i < MAX_SNACKS_ITEMS; i++)
    {
       // $(outsideDiv).append('<li class="filler"><img class="side_picture filler_text" src="' + OUTLET_URL + '/order_app/image/' + price_data[item_id]["master_id"] + '" /></li>');
    }
}

function shouldItemBeVisible(item_id)
{
    return simpleStorage.get(item_id + "_visibility");
}

function handleStockCountData(data)
{
    var stock_count = data;
    var dispenserDiv;
    if(item_images) {
         dispenserDiv = $("#dispenser_items_TATA");
    }
    else {
        dispenserDiv = $("#dispenser_items");
    }
    // Cleaning up the div before filling items
    $(dispenserDiv).empty();
    var counter = 0;
    sortedList = sortStockData(stock_count);
    sortedList.map(function (item)
    {
        // subtracting the locked items
        var displayable_count = getStockItemCount(item[1]["item_details"]) - item[1]["locked_count"];
        // This item belongs to test mode, so overwriting everything
        if (isTestModeItem(parseInt(item[0])))
        {
            if (!TEST_MODE)
            {
                return;
            }
            if (displayable_count > 0)
            {
                $(dispenserDiv).append('<li onclick="showPopup(this);" class="veg"><div class="item_code" data-item-code="' + item[0] + '">' + item[0] + '</div></li>');
                counter++;
            }
            price_data[item[0]] = {};
            price_data[item[0]]["stock_quantity"] = displayable_count;
            return;
        }

        if (!price_data.hasOwnProperty(item[0]))
        {
            return;
        }
        var veg = price_data[item[0]]["veg"];
        price_data[item[0]]["stock_quantity"] = displayable_count;
        var veg_class;
        if(item_images) {
            veg_class = veg ? "veg_TATA" : "non-veg";
        }
        else  {
            veg_class = veg ? "veg" : "non-veg";
        }

        if (displayable_count > 0)
        {
            if (counter < MAX_MEAL_ITEMS)
            {
                // populating the items in main section
                if(item_images) {
                    $(dispenserDiv).append('<li onclick="showPopup(this);" class="' + veg_class + '"> <img class="item_code_TATA" data-item-code="' + item[0] + '" src="' + OUTLET_URL + '/order_app/image/' + price_data[item[0]]["master_id"] + '" /> <div class="item_code_TATA" onclick="showPopup(this) data-item-code="' + item[0] + '">' + price_data[item[0]]["item_tag"] + '</div> </li>');
                }
                else {
                    $(dispenserDiv).append('<li onclick="showPopup(this);" class="' + veg_class + '"> <img class="item_code" data-item-code="' + item[0] + '" src="' + OUTLET_URL + '/order_app/image/' + price_data[item[0]]["master_id"] + '" /> <div class="item_code" data-item-code="' + item[0] + '">' + price_data[item[0]]["item_tag"] + '</div> </li>');
                }
                counter++;
            }
        }
    });

    for (var i = counter; i < MAX_MEAL_ITEMS; i++)
    {
       // $(dispenserDiv).append('<li class="filler"><div class="item_code filler_text">0</div></li>');
    }

    // If some items got removed set their stock_quantity to 0
    /*for (var key in price_data) {
    if (! isAvailableInStock(key, stock_count) && ! (key in current_order)) {
    price_data[key]["stock_quantity"] = 0;
    } else if (! isAvailableInStock(key, stock_count) && (key in current_order)) {
    price_data[key]["stock_quantity"] = current_order[key][0];;
    }
    }*/

    if(SHOW_SNACKS) {
        $("#selector_buttons").show();
    }
    else {
        $("#selector_buttons").hide();
    }
}

function sortStockData(stock_count)
{
    var sortedList = [];
    if (!stock_count)
    {
        return sortedList;
    }
    sortedList = Object.keys(stock_count).map(function (key) { return [key, stock_count[key]]; });

    sortedList.sort(function (a, b)
    {
        if (!price_data.hasOwnProperty(a[0]) || !price_data.hasOwnProperty(b[0]))
        {
            return 0;
        }
        if (price_data[a[0]]["veg"] != price_data[b[0]]["veg"])
        {
            if (price_data[a[0]]["veg"] < price_data[b[0]]["veg"])
            {
                return 1;
            } else
            {
                return -1;
            }
        } else
        {
            return (Number(a[0]) - Number(b[0]));
        }
    });

    return sortedList;
}

function isAvailableInStock(key, stock_count)
{
    // stock_count is null
    if (!stock_count)
    {
        return false
    }
    // key is not there
    if (!(key in stock_count))
    {
        return false;
    }
    // key is there but all items are locked
    if (getStockItemCount(stock_count[key]["item_details"]) - stock_count[key]["locked_count"] <= 0)
    {
        return false;
    }
    return true;
}

function getStockItemCount(item_details)
{
    var count = 0;
    for (var i = 0; i < item_details.length; i++)
    {
        if (!item_details[i]["expired"] && !item_details[i]["spoiled"])
        {
            count += item_details[i]["count"];
        }
    }
    return count;
}

function showPopup(scope)
{
    // This function can be called from order list as well as main section items
    // Hence the type checking
    $("#main_alert_text").css("visibility", "hidden");
    if (typeof (scope) == "object")
    {
        var item_code;
        if(!item_images) {
            if (in_dispenser)
            {
                 item_code = parseInt($(scope).children(".item_code").first().attr("data-item-code"));
            } else
            {
                 item_code = parseInt($(scope).children("img").first().attr("data-item-code"));
            }
        }
        else {
            //alert("TATA")
            if (in_dispenser)
             {
                  item_code = parseInt($(scope).children(".item_code_TATA").first().attr("data-item-code"));
             } else
             {
                  item_code = parseInt($(scope).children("img").first().attr("data-item-code"));
             }
        }
    } else
    {
        var item_code = scope;
    }
    $("#item_popup #item_code").val(item_code);
    // If current_order has a previous value, show that, else default to 1
    if (current_order.hasOwnProperty(item_code))
    {
        // This is editing an order
        $("#item_popup #quantity").text(current_order[item_code][0]);
        original_quantity = current_order[item_code][0];
    } else
    {
        // Adding a new item, therefore locking it
        if (isItemInDispenser(item_code))
        {
            $("#increase").unbind("click");
            $("#decrease").unbind("click");
            tryLockItem(item_code, function (response)
            {
                $("#increase").bind("click", onIncreaseClick);
                $("#decrease").bind("click", onDecreaseClick);
                if (response.error)
                {
                    $("#item_popup #quantity").text(0);
                    original_quantity = 0;
                    return;
                }
                if (response.available)
                {
                    $("#item_popup #quantity").text(1);
                    original_quantity = 1;
                } else
                {
                    $("#item_popup #quantity").text(0);
                    original_quantity = 0;
                }
            });
        } else
        {
            $("#item_popup #quantity").text(1);
            original_quantity = 1;
        }
    }

    // Checking if this is a test mode item
    if (isTestModeItem(item_code))
    {
        $("#item_popup").removeClass("fixtop");
        $("#coke_area").css("display", "none");
        $("#coke_quantity").text(0);
        $("#coke_price").hide();
        $("#coke_picture").hide();
        $("#coke_button").hide();
        $("#item_popup #main_picture").attr("src", OUTLET_URL + '/order_app/image/');
    } else
    {
        if (price_data[item_code]["coke_details"]["mrp"])
        {
            $("#item_popup").addClass("fixtop");
            $("#coke_price").text('Rs. ' + (price_data[item_code]["coke_details"]["mrp"] * price_data[item_code]["coke_details"]["discount_percent"] / 100));
            if (current_order.hasOwnProperty(item_code))
            {
                $("#coke_quantity").text(current_order[item_code][1]);
            } else
            {
                $("#coke_quantity").text(0);
            }
            $("#alert_text").css("visibility", "hidden");
            $("#coke_area").css("display", "block");
            $("#coke_price").show();
            $("#coke_picture").show();
            $("#coke_button").show();
        } else
        {
            $("#item_popup").removeClass("fixtop");
            $("#coke_area").css("display", "none");
            $("#coke_quantity").text(0);
            $("#coke_price").hide();
            $("#coke_picture").hide();
            $("#coke_button").hide();
        }
        $("#item_popup #main_picture").attr("src", OUTLET_URL + '/order_app/image/' + price_data[item_code]["master_id"]);
    }


    $("#item_popup").foundation('reveal', 'open',
          { close_on_background_click: false, animation: 'none' });
    $('div').filter('[data-role="page"]').addClass("grayscale");
}

function isTestModeItem(item_code)
{
       if (item_code >= 9000 && item_code <= 9004)
        {
          return true;
        } else
       {
           return false;
       }

}

function updateOrderSummary()
{
    // get the values from current_order and repopulate the order pane
    var tableDiv = $("#order_summary table tbody");
    $(tableDiv).empty();
    var total_amount = 0;
    $("#total_amount .rupee").hide();
    var decimal_Count=0;
      if(simpleStorage.get("COUNTRY_TYPE").toString().toLowerCase()!='india')
      {
       decimal_Count=2;
      }

    for (var key in current_order)
    {
        var quantity = current_order[key][0];
        var coke_quantity = current_order[key][1];

        if (isTestModeItem(key))
        {
            var price = 1;
        } else
        {
            var price = price_data[key]["mrp"] * quantity;
        }
        total_amount += price;


        if (current_order[key][2] === "dispenser")
        {
            if (isTestModeItem(key))
            {
                $(tableDiv).append('<tr onclick=showPopup(' + key + ');><td><img src="img/edit.png" /></td><td class="d_item">' + key + '</td><td>' + quantity + '</td><td class="right_justified"><div class="rupee">'+CURRENCY_SYM+' </div>' + price.toFixed(decimal_Count) + '</td></tr>');
            } else
            {
                $(tableDiv).append('<tr onclick=showPopup(' + key + ');><td><img src="img/edit.png" /></td><td class="d_item">' + price_data[key]["name"] + '</td><td>' + quantity + '</td><td class="right_justified"><div class="rupee">'+CURRENCY_SYM+' </div>' + price.toFixed(decimal_Count) + '</td></tr>');
            }
        } else
        {
            $(tableDiv).append('<tr onclick=showPopup(' + key + ');><td><img src="img/edit.png" /></td><td>[' + price_data[key]["name"] + ']</td><td>' + quantity + '</td><td class="right_justified"><div class="rupee">'+CURRENCY_SYM+' </div>' + price.toFixed(decimal_Count) + '</td></tr>');
        }
        if (coke_quantity)
        {
            var coke_price = (price_data[key]["coke_details"]["mrp"] * price_data[key]["coke_details"]["discount_percent"] / 100) * coke_quantity;
            $(tableDiv).append('<tr onclick=showPopup(' + key + ');><td><img src="img/edit.png" /></td><td>+coke</td><td>' + coke_quantity + '</td><td class="right_justified"><div class="rupee">'+CURRENCY_SYM+' </div>' + coke_price.toFixed(decimal_Count) + '</td></tr>');
            total_amount += coke_price;
        }
        $("#total_amount .rupee").show();
    }
    // Showing the total amount only when it is required
    if (!total_amount)
    {
        $("#total_amount .num").hide();
    } else
    {
        $("#total_amount .num").show();
        $("#total_amount .num").text(total_amount.toFixed(decimal_Count));
    }


}

function removeLock(item_code)
{
    var quantity_details = current_order[item_code];
    if (!quantity_details)
    {
        // item was never added to order
        return;
    }
    // deleting the item from the current order
    delete current_order[item_code];
}

function clearAllLocks()
{
/* delivery misses changes decreaseCL*/
    $.ajax({
        url: OUTLET_URL + '/menu_display/stock_initial/',
        dataType: 'json',
        timeout: 3000, //3 second timeout
        success: function (stock_count)
        {
            for (var item_id in stock_count)
            {
                $.post(OUTLET_URL + '/order_app/lock_item/' + item_id,

          { "direction": "decreaseCL", "delta_count": Number(stock_count[item_id]["locked_count"]) -(isNaN(Number(stock_count[item_id]["mobile_locked_count"]))?0:Number(stock_count[item_id]["mobile_locked_count"])) })
          .done(function ()
          {
          })
          .fail(function ()
          {
              console.error("Error occured while removing the lock for item- " + item_id);
          });
            }
        },
        error: function (jqxhr, textStatus, error)
        {
            var err_msg = textStatus + ", " + error;
            console.error("Request Failed: " + err_msg);
        }
    });
}

function updateLock(item_code, quantity, coke_quantity)
{
    if (item_code in current_order)
    {
        var delta_items = quantity - current_order[item_code][0];
    } else
    {
        var delta_items = quantity;
    }
    if (isTestModeItem(item_code))
    {
        var location = "dispenser";
    } else
    {
        var location = price_data[item_code]["location"];
    }
    // setting the current order to the new quantity
    current_order[item_code] = [quantity, coke_quantity, location];
}

function increaseLockByOne(item_id)
{
    increaseLockByQty(item_id, 1);
}

function increaseLockByQty(item_id, qty)
{
    $.post(OUTLET_URL + '/order_app/lock_item/' + item_id,
    { "direction": "increase", "delta_count": qty })
    .done(function ()
    {
    })
    .fail(function ()
    {
        console.error("Error occured while creating the lock for item- " + item_code);
    });
}

function decreaseLockByOne(item_id)
{
    decreaseLockByQty(item_id, 1);
}

function decreaseLockByQty(item_id, qty)
{
    console.log("Decreasing lock for item " + item_id + " by " + qty);
    $("#decrease").unbind("click");
    $("#increase").unbind("click");
    $.ajax({
        type: 'POST',
        timeout: 3000,
        url: OUTLET_URL + '/order_app/lock_item/' + item_id,
        data: JSON.stringify({ "direction": "decrease", "delta_count": qty }),
        success: function ()
        {
            $("#increase").bind("click", onIncreaseClick);
            $("#decrease").bind("click", onDecreaseClick);
            console.log("Successfully unlocked item - " + item_id);
        },
        error: function (jqxhr, textStatus, error)
        {
            $("#increase").bind("click", onIncreaseClick);
            $("#decrease").bind("click", onDecreaseClick);
            var err_msg = textStatus + ", " + jqxhr.responseText;
            console.error("Error occured while decreasing lock: " + err_msg);
        },
        contentType: "application/json",
        dataType: 'text'
    });
}

function tryLockItem(item_id, callback)
{
    $.ajax({
        type: 'POST',
        timeout: 3000,
        url: OUTLET_URL + '/order_app/try_lock/' + item_id,
        data: JSON.stringify({ "delta_count": 1 }),
        success: function (data)
        {
            console.log("Successfully locked item - " + item_id);
            data = JSON.parse(data);
            callback({ error: data.error, available: data.flag, stale: data.stale });
        },
        error: function (jqxhr, textStatus, error)
        {
            var err_msg = textStatus + ", " + error;
            console.error("Error occured while trying to acquire lock: " + err_msg);
            callback({ error: true });
        },
        contentType: "application/json",
        dataType: 'text'
    });
}

function removeOrderLock(current_order)
{
    for (var key in current_order)
    {
        (function (item_code, new_value)
        {
            if (isTestModeItem(item_code))
            {
                var jqxhr = $.post(OUTLET_URL + '/order_app/lock_item/' + item_code,
          { "direction": "decrease", "delta_count": new_value })
          .done(function ()
          {
          })
          .fail(function ()
          {
              console.error("Error occured while removing the lock for item- " + item_code);
          });
                return;
            }
            // lock an item only if it is dispenser
            if (price_data[item_code]["location"] == "dispenser")
            {
                var jqxhr = $.post(OUTLET_URL + '/order_app/lock_item/' + item_code,
          { "direction": "decrease", "delta_count": new_value })
          .done(function ()
          {
          })
          .fail(function ()
          {
              console.error("Error occured while removing the lock for item- " + item_code);
          });
            }
        })(key, current_order[key][0]);
        delete current_order[key];
    }
    updateOrderSummary();
}


function generateUUID() {

var currentDate = new Date();

var month = currentDate.getMonth()+1;
var day = currentDate.getDate();

var currentDateOutput = currentDate.getFullYear() +
    ((''+month).length<2 ? '0' : '') + month +
    ((''+day).length<2 ? '0' : '') + day;

    var d = currentDate.getTime();
    var uuid = 'xxxxxxxxx'.replace(/[x]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return currentDateOutput+uuid;
};


function pushOrder(mode, savings, mobile_num, creditCardNo, cardHolderName)
{
  console.log("-------------------------------pushOrder called");
    var order_to_send = {};
    var sides = {};
    var old_current_order = current_order;
    // push current order with an ajax call (only dispenser items are in the order_to_send variable.
    // Other items are required for bill printing)
    var uniqueRandomId= generateUUID();
    for (var item_id in current_order)
    {
        if (current_order[item_id][2] == "dispenser")
        {
            if (isTestModeItem(item_id))
            {
                order_to_send[item_id] = { "count": current_order[item_id][0],
                    "price": 1 * current_order[item_id][0],
                    "heating_flag": false,
                    "name": item_id.toString(),
                    "restaurant_details":
          { "id": 1, "tin_no": 1212, "st_no": 1212, "name": "dummy" },
                    "side_order": "no side order",
                     "veg": price_data[item_id]["veg"]
                };
            } else
            {
                order_to_send[item_id] = { "count": current_order[item_id][0],
                    "price": price_data[item_id]["mrp"] * current_order[item_id][0],
                    "heating_flag": price_data[item_id]["heating_reqd"],
  		    "heating_reduction": price_data[item_id]["heating_reduction"],
   	            "condiment_slot": price_data[item_id]["condiment_slot"],
                    "name": price_data[item_id]["name"],
                    "restaurant_details": price_data[item_id]["restaurant_details"],
                    "side_order": price_data[item_id]["side_order"],
                    "veg": price_data[item_id]["veg"]
                };
            }
            if (current_order[item_id][1])
            {
                var coke_price = price_data[item_id]["coke_details"]["mrp"] * price_data[item_id]["coke_details"]["discount_percent"] / 100;
                sides[price_data[item_id]["coke_details"]["id"]] = {
                    "name": price_data[item_id]["coke_details"]["name"],
                    "price": coke_price * current_order[item_id][1],
                    "count": current_order[item_id][1],
                    "restaurant_details": price_data[item_id]["coke_details"]["restaurant_details"],
                    "side_order": price_data[item_id]["side_order"],
                     "veg": price_data[item_id]["veg"]
                }
            }
        } else
        {
            sides[item_id] = {
                "name": price_data[item_id]["name"],
                "count": current_order[item_id][0],
                "price": price_data[item_id]["mrp"] * current_order[item_id][0],
                "restaurant_details": price_data[item_id]["restaurant_details"],
                "side_order": price_data[item_id]["side_order"],
                 "veg": price_data[item_id]["veg"]
            };
        }
    }
    // clear out current order
    for (var item_id in current_order)
    {
        delete current_order[item_id];
    }
    updateOrderSummary();
    var countrytype=simpleStorage.get("COUNTRY_TYPE");
    $.retryAjax({
        type: 'POST',
        timeout: 5000,
        retryLimit: 5,
        url: OUTLET_URL + '/order_app/place_order',
        data: JSON.stringify({ "order": order_to_send,
            "sides": sides,
            "counter_code": COUNTER_CODE,
            "mode": mode,
            "from_counter": false,
            "savings": savings,
            "mobile_num": mobile_num,
            "credit_card_no": creditCardNo,
            "cardholder_name": cardHolderName,
            "test_mode": TEST_MODE,
            "unique_Random_Id":uniqueRandomId,
            "countrytype":countrytype
        }),
        success: function (data)
        {
            console.log(data);
            data = JSON.parse(data);
            var bill_no = data.bill_no;
            if (mode != "card")
            {
                $("#cash_checkout .payment_area .order_no").text(bill_no);
                $('#cash_checkout').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
                $('div').filter('[data-role="page"]').addClass("grayscale");
                // closing the dialog after 5 secs
                setTimeout(function ()
                {
                    $('#cash_checkout').foundation('reveal', 'close');
                    $('div').filter('[data-role="page"]').removeClass("grayscale");
                }, 5000);
            } else
            {
             console.log("-------------------------------Invoke success card transaction called here");
                $("#card_success .payment_area .order_no").text(bill_no);
                $('#card_success').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
                $('div').filter('[data-role="page"]').addClass("grayscale");
                setTimeout(function ()
                {
                    $('#card_success').foundation('reveal', 'close');
                    $('div').filter('[data-role="page"]').removeClass("grayscale");
                     console.log("-------------------------------Invoke success card transaction killed here");
                }, 5000); // auto closing after 10 secs
            }
        },
        error: function (jqxhr, textStatus, error)
        {
         console.log("-------------------------------Error in Transaction");
            var err_msg = textStatus + ", " + jqxhr.responseText;
            current_order = old_current_order;
            updateOrderSummary();
            console.error("Place order failed: " + err_msg);
            // Showing apprpriate message to the screen
            $("#card_failure .title").text("");
            $("#failure_reason").text("Outlet seems to have connectivity issues");
            // Closing the card dialog first
            $('#card_checkout').foundation('reveal', 'close');
            $('#card_failure').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
            $('div').filter('[data-role="page"]').addClass("grayscale");
        },
        contentType: "application/json",
        dataType: 'text'
    });
}

function saveSettings()
{
    SETTINGS_PASSWORD = $("#settings_passwd").val();
    simpleStorage.set("SETTINGS_PASSWORD", SETTINGS_PASSWORD);

    simpleStorage.set("HQ_URL", $("#hq_url").val());
    HQ_URL = $("#hq_url").val();
    simpleStorage.set("OUTLET_URL", $("#outlet_url").val());
    OUTLET_URL = $("#outlet_url").val();

    WEBSOCKET_URL = $("#websocket_url").val();
    simpleStorage.set("WEBSOCKET_URL", WEBSOCKET_URL);

    simpleStorage.set("OUTLET_ID", $("#outlet_id").val());
    OUTLET_ID = $("#outlet_id").val();
    simpleStorage.set("COUNTER_CODE", $("#counter_code").val());
    COUNTER_CODE = $("#counter_code").val();
    simpleStorage.set("ACCEPT_CREDIT_CARDS", $("#accept_cards").is(":checked"));
    ACCEPT_CREDIT_CARDS = $("#accept_cards").is(":checked");
    simpleStorage.set("ACCEPT_CASH", $("#accept_cash").is(":checked"));
    ACCEPT_CASH = $("#accept_cash").is(":checked");
    simpleStorage.set("SHOW_SNACKS", $("#show_snacks").is(":checked"));
    SHOW_SNACKS = $("#show_snacks").is(":checked");

    simpleStorage.set("MOBILE_MANDATORY", $("#mobile_mandatory").is(":checked"));
    MOBILE_MANDATORY = $("#mobile_mandatory").is(":checked");

    simpleStorage.set("item_images", $("#item_images").is(":checked"));
    item_images = $("#item_images").is(":checked");

    simpleStorage.set("OTHERS_MANDATORY", $("#others_mandatory").is(":checked"));
    OTHERS_MANDATORY = $("#others_mandatory").is(":checked");


     simpleStorage.set("COUNTRY_TYPE", $( "#country_type option:selected" ).text());
     COUNTRY_TYPE = $( "#country_type option:selected" ).text();
     simpleStorage.set("PAYMENT_TYPE", $( "#payment_type option:selected" ).text());
     PAYMENT_TYPE = $( "#payment_type option:selected" ).text();

    simpleStorage.set("MSWIPE_USERNAME", $("#mswipe_username").val());
    MSWIPE_USERNAME = $("#mswipe_username").val();
    simpleStorage.set("MSWIPE_PASSWORD", $("#mswipe_password").val());
    MSWIPE_PASSWORD = $("#mswipe_password").val();

    simpleStorage.set("INSPIRENETZ_DIGEST_AUTH", $("#inspire_digest_auth").val());
    INSPIRENETZ_DIGEST_AUTH = $("#inspire_digest_auth").val();
    simpleStorage.set("INSPIRENETZ_USERNAME", $("#inspire_username").val());
    INSPIRENETZ_USERNAME = $("#inspire_username").val();
    simpleStorage.set("INSPIRENETZ_PASSWORD", $("#inspire_password").val());
    INSPIRENETZ_PASSWORD = $("#inspire_password").val();
    simpleStorage.set("INSPIRENETZ_HTTP_URL", $("#inspire_http_url").val());
    INSPIRENETZ_HTTP_URL = $("#inspire_http_url").val();

   simpleStorage.set("MERCHANT_ID", $('#Ongo_Merchant_ID').val());
   simpleStorage.set("TERMINAL_ID", $('#Ongo_Terminal_ID').val());
   simpleStorage.set("BLUETOOTH_NAME",$('#Ongo_Blutooth_Name').val());
   simpleStorage.set("BLUETOOTH_ADDRESS", $('#Ongo_Blutooth_Addrs').val());

   simpleStorage.set("IP_ADDRESS",$('#Singapore_IP_Address').val());
   simpleStorage.set("PORT_NUMBER", $('#Singapore_Port_Number').val());
// pinelabs local storage settings
   simpleStorage.set("PINE_MERCHANT_ID",$('#PineLabs_Merchant_ID').val());
   simpleStorage.set("SECURITY_TOKEN", $('#PineLabs_Security_Token').val());
    SECURITY_TOKEN = $("#PineLabs_Security_Token").val();
   simpleStorage.set("STORE_POS_CODE",$('#PineLabs_Store_Pos_Code').val());
   STORE_POS_CODE = $("#PineLabs_Store_Pos_Code").val();
   simpleStorage.set("IMEI",$('#PineLabs_IMEI').val());
   simpleStorage.set("USER_ID", $('#PineLabs_User_ID').val());
   USER_ID = $("#PineLabs_User_ID").val();
   simpleStorage.set("TRANSACTION_NUMBER",$('#PineLabs_TransactionNumber').val());
   TRANSACTION_NUMBER = $("#PineLabs_TransactionNumber").val();

    snacksVisibility();

    Android.saveSettings(INSPIRENETZ_DIGEST_AUTH, INSPIRENETZ_USERNAME,
    INSPIRENETZ_PASSWORD, INSPIRENETZ_HTTP_URL);
}

function showFailureScreen(displayMsg)
{
    $("#failure_reason").text(displayMsg);
    // Closing the card dialog first
    $('#card_checkout').foundation('reveal', 'close');
    $("#card_failure .title").text("TRANSACTION STATUS");
     $('#card_failure .first_line').text("");
    $('#card_failure').foundation('reveal', 'open', { close_on_background_click: false, animation: 'none' });
    $('div').filter('[data-role="page"]').addClass("grayscale");
}

function showSuccessScreen(creditCardNo, cardHolderName)
{
   console.log("-------------------------------showSuccessScreen called",creditCardNo+cardHolderName);
    // Get the order details and push order from here
    var order_to_push = simpleStorage.get("order");
    pushOrder(order_to_push.mode, order_to_push.savings, order_to_push.mobile_num, creditCardNo, cardHolderName);
    console.log("-------------------------------showSuccessScreen Killed");
    // Opening the card success screen
    $('#card_checkout').foundation('reveal', 'close');
}

function showBankSummary(saleAmount, saleCount, summaryDate)
{
    var saleAmountDiv = $("<div>Sale amount - " + saleAmount + "</div>");
    var saleCountDiv = $("<div>Sale count -" + saleCount + "</div>");
    var summaryDateDiv = $("<div>Summary Date -" + summaryDate + "</div>");
    $("#settingsModal #status_text").empty();
    $("#settingsModal #status_text").append(saleAmountDiv);
    $("#settingsModal #status_text").append(saleCountDiv);
    $("#settingsModal #status_text").append(summaryDateDiv);
}

function snacksVisibility()
{
    if (SHOW_SNACKS)
    {
        $("#selector_button").show();
    } else
    {
        $("#selector_button").hide();
    }
}

function updateSavings(mobile_num, total_expenditure, total_savings)
{
    $.ajax({
        type: 'POST',
        timeout: 3000,
        url: OUTLET_URL + '/order_app/customer_details/' + mobile_num,
        data: JSON.stringify({ "total_expenditure": total_expenditure,
            "total_savings": total_savings
        }),
        success: function (data) { console.log(data); },
        error: function (jqxhr, textStatus, error)
        {
            var err_msg = textStatus + ", " + jqxhr.responseText;
            console.error("Updating savings failed: " + err_msg);
        },
        contentType: "application/json",
        dataType: 'text'
    });
}

function populateSides()
{
    var sideOrderList = [];
    for (var item_id in current_order)
    {
        if (!price_data.hasOwnProperty(item_id))
        {
            continue;
        }
        if (price_data[item_id]["side_order"])
        {
            // This means this item has a side order
            sideOrderList.push(" " + current_order[item_id][0] + " " + price_data[item_id]["side_order"] + " ");
        }
    }

    if (sideOrderList.length == 0)
    {
        $(".sides .first_line").text("Please enjoy your meal !");
    } else
    {
        var sideOrderText = sideOrderList.join(',');
        sideOrderText = "Be sure to pick up " + sideOrderText;
        $(".sides .first_line").text(sideOrderText);
    }


/*function currencyLocalization(cur_value)
{
return (""+cur_value);
}*/
}
