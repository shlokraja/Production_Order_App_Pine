package com.ftl.mswipeinterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ftl.paymentgateway.DeviceState;
import com.ftl.paymentgateway.MSwipeGateway;
import com.ftl.paymentgateway.PaymentGateway;
import com.ftl.paymentgateway.PaymentRequest;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// Class to respond to web app clicks and take appropriate actions
public class WebAppInterface {
    Context mContext;
    WebView mWebview;
    private MyLogger logger;
    /** Instantiate the interface and set the context */
    WebAppInterface(Context c, WebView v) {
        mContext = c;
        mWebview = v;
        logger = MyLogger.getInstance();
    }

    @JavascriptInterface
    public void initiateConnection() {
        logger.debug(MyActivity.TAG, "Initiating connection", mContext);
        logger.debug(MyActivity.TAG, "Connecting to device result is - " + ((MyActivity)mContext).connectToDevice(false), mContext);
    }

    @JavascriptInterface
    public void initiateDisconnection() {
        ((MyActivity)mContext).disconnectDevice();
    }

    @JavascriptInterface
    public void initiateAuthentication(String mswipe_username, String mswipe_password) {
        // Authenticating with the supplied username and password
        MSwipeGateway.wisePadController.AuthenticateMerchant(MSwipeGateway.loginHandler,
                mswipe_username,
                mswipe_password);
    }

    @JavascriptInterface
    public void initiateCheckCard(String total_money, String test_mode, String mobile_no) {
        // XXX: bad hack. Lack of time
        MSwipeGateway.test_mode = Boolean.valueOf(test_mode);
        logger.debug(MyActivity.TAG, "Value of test_mode is " + test_mode, mContext);
        logger.debug(MyActivity.TAG, "Storing the mobile no " + mobile_no, mContext);
        SharedPreferences sharedPref = ((MyActivity)mContext).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(MSwipeGateway.WISEPAD_MOBILE_NO, "+91"+mobile_no).commit();
        ((MyActivity)mContext).checkCard(total_money);
    }

    @JavascriptInterface
    public void initiateCancelCheckCard() {
        logger.debug(MyActivity.TAG, "Initiating cancel CheckCard", mContext);
        ((MyActivity)mContext).cancelCheckCard();
    }

    @JavascriptInterface
    public void saveSettings(String digest_auth, String username, String password, String http_url) {
        logger.info(MyActivity.TAG, "Received settings. digest_auth - " + digest_auth
                + " username - " + username
                + " password - " + password
                + " http_url - " + http_url, mContext);
        // Storing the details in a SharedPreference
        SharedPreferences sharedPref = ((MyActivity)mContext).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(MSwipeGateway.INSPIRENETZ_DIGEST_AUTH, digest_auth).commit();
        editor.putString(MSwipeGateway.INSPIRENETZ_USERNAME, username).commit();
        editor.putString(MSwipeGateway.INSPIRENETZ_PASSWORD, password).commit();
        editor.putString(MSwipeGateway.INSPIRENETZ_HTTP_URL, http_url).commit();
    }

    @JavascriptInterface
    public void initiateLogFileRead() {
        ((MyActivity)mContext).showLogOutput();
    }

    @JavascriptInterface
    public void initiateShowBankSummary() {
        ((MyActivity)mContext).showBankSummary();
    }

    @JavascriptInterface
    public void mailLogs() {
        ((MyActivity)mContext).mailLogs();
    }

    @JavascriptInterface
    public void getPaymentGatewayTypes()
    {
        ((MyActivity)mContext).setPaymentGatewayTypes();
    }

    @JavascriptInterface
    public void setSelectedPaymentGateway(String selectedPaymentGateway)
    {
        ((MyActivity)mContext).setSelectedPaymentGateway(selectedPaymentGateway,null);
    }

    @JavascriptInterface
    public void initiateOngoAuthentication(String selectedType,String blu_name, String blu_addrs,String ter_id, String mer_id) {
        PaymentRequest request=new PaymentRequest();
        request.Bluetooth_id=blu_addrs;
        request.Bluetooth_name=blu_name;
        request.Merchant_id=mer_id;
        request.Terminal_id=ter_id;
        ((MyActivity)mContext).setSelectedPaymentGateway(selectedType,request);
    }

    @JavascriptInterface
    public void initiateSingporeAuthentication(String selectedType,String ip_address,String port_no) {
        PaymentRequest request=new PaymentRequest();
        request.Ip_Address=ip_address;
        request.Port_Number=port_no;
        ((MyActivity)mContext).setSelectedPaymentGateway(selectedType,request);
    }

    @JavascriptInterface
    public void initiatePineLabsAuthentication(String selectedType,String merchant_ID, String security_Token,String store_Pos_Code,
                                               String IMEI,String User_ID,String transaction_Number) {
        PaymentRequest request=new PaymentRequest();
        request.Merchant_id=merchant_ID;
        request.Security_Token=security_Token;
        request.Store_Pos_Code=store_Pos_Code;
        request.IMEI=IMEI;
        request.User_ID=User_ID;
        request.Transaction_Number=transaction_Number;
        ((MyActivity)mContext).setSelectedPaymentGateway(selectedType,request);
    }
}
