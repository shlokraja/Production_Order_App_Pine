SETTINGS_PASSWORD_original = "123";
var value = simpleStorage.get("SETTINGS_PASSWORD");
if(!value){
    simpleStorage.set("SETTINGS_PASSWORD", SETTINGS_PASSWORD_original);
    SETTINGS_PASSWORD = SETTINGS_PASSWORD_original;
} else {
  SETTINGS_PASSWORD = value;
}

HQ_URL_original = "http://115.114.95.35:8008";
OUTLET_URL_original = "http://192.168.1.47:8000";
WEBSOCKET_URL_original = OUTLET_URL_original.substr(0,OUTLET_URL_original.length-5) + ":8000";
OUTLET_ID_original = 21;
COUNTER_CODE_original = 1;

/*ACCEPT_CREDIT_CARDS_original = true;
SHOW_SNACKS_original = true;
SHOW_LOGS_original = true;*/

MSWIPE_USERNAME_original = "9444126325";
MSWIPE_PASSWORD_original = "mswipe";

INSPIRENETZ_DIGEST_AUTH_original = "www.inspirenetz.com";
INSPIRENETZ_USERNAME_original = "atc_api_user1";
INSPIRENETZ_PASSWORD_original = "@tcap1@741";
INSPIRENETZ_HTTP_URL_original = "http://www.inspirenetz.com/api/0.8/json";

// Check if "key" exists in the storage
var value = simpleStorage.get("HQ_URL");
if(!value){
    simpleStorage.set("HQ_URL",HQ_URL_original);
    HQ_URL = HQ_URL_original;
} else {
  HQ_URL = value;
}

var value = simpleStorage.get("OUTLET_URL");
if(!value){
    simpleStorage.set("OUTLET_URL",OUTLET_URL_original);
    OUTLET_URL = OUTLET_URL_original;
} else {
  OUTLET_URL = value;
}

var value = simpleStorage.get("WEBSOCKET_URL");
if(!value){
    simpleStorage.set("WEBSOCKET_URL",WEBSOCKET_URL_original);
    WEBSOCKET_URL = WEBSOCKET_URL_original;
} else {
  WEBSOCKET_URL = value;
}

var value = simpleStorage.get("OUTLET_ID");
if(!value){
    simpleStorage.set("OUTLET_ID",OUTLET_ID_original);
    OUTLET_ID = OUTLET_ID_original;
} else {
  OUTLET_ID = value;
}
var value = simpleStorage.get("COUNTER_CODE");
if(!value){
    simpleStorage.set("COUNTER_CODE",COUNTER_CODE_original);
    COUNTER_CODE = COUNTER_CODE_original;
} else {
  COUNTER_CODE = value;
}

var value = simpleStorage.get("ACCEPT_CREDIT_CARDS");
ACCEPT_CREDIT_CARDS = value;

var value = simpleStorage.get("ACCEPT_CASH");
ACCEPT_CASH = value;

var mobi_value=simpleStorage.get("MOBILE_MANDATORY");
MOBILE_MANDATORY=mobi_value;

var others_value=simpleStorage.get("OTHERS_MANDATORY");
OTHERS_MANDATORY=others_value;

var country=simpleStorage.get("COUNTRY_TYPE");
COUNTRY_TYPE=country;
var paymentType=simpleStorage.get("PAYMENT_TYPE");

PAYMENT_TYPE=paymentType;

var value = simpleStorage.get("SHOW_SNACKS");
SHOW_SNACKS = value;

var value = simpleStorage.get("SHOW_LOGS");
SHOW_LOGS = value;

var value = simpleStorage.get("item_images");
item_images = value;

var value = simpleStorage.get("MSWIPE_USERNAME");
if(!value){
    simpleStorage.set("MSWIPE_USERNAME",MSWIPE_USERNAME_original);
    MSWIPE_USERNAME = MSWIPE_USERNAME_original;
} else {
  MSWIPE_USERNAME = value;
}
var value = simpleStorage.get("MSWIPE_PASSWORD");
if(!value){
    simpleStorage.set("MSWIPE_PASSWORD", MSWIPE_PASSWORD_original);
    MSWIPE_PASSWORD = MSWIPE_PASSWORD_original;
} else {
  MSWIPE_PASSWORD = value;
}

var value = simpleStorage.get("INSPIRENETZ_DIGEST_AUTH");
if(!value){
    simpleStorage.set("INSPIRENETZ_DIGEST_AUTH", INSPIRENETZ_DIGEST_AUTH_original);
    INSPIRENETZ_DIGEST_AUTH = INSPIRENETZ_DIGEST_AUTH_original;
} else {
  INSPIRENETZ_DIGEST_AUTH = value;
}
var value = simpleStorage.get("INSPIRENETZ_USERNAME");
if(!value){
    simpleStorage.set("INSPIRENETZ_USERNAME", INSPIRENETZ_USERNAME_original);
    INSPIRENETZ_USERNAME = INSPIRENETZ_USERNAME_original;
} else {
  INSPIRENETZ_USERNAME = value;
}
var value = simpleStorage.get("INSPIRENETZ_PASSWORD");
if(!value){
    simpleStorage.set("INSPIRENETZ_PASSWORD", INSPIRENETZ_PASSWORD_original);
    INSPIRENETZ_PASSWORD = INSPIRENETZ_PASSWORD_original;
} else {
  INSPIRENETZ_PASSWORD = value;
}
var value = simpleStorage.get("INSPIRENETZ_HTTP_URL");
if(!value){
    simpleStorage.set("INSPIRENETZ_HTTP_URL", INSPIRENETZ_HTTP_URL_original);
    INSPIRENETZ_HTTP_URL = INSPIRENETZ_HTTP_URL_original;
} else {
  INSPIRENETZ_HTTP_URL = value;
}

// Setting the global variable of test mode to be intially false
TEST_MODE = false
