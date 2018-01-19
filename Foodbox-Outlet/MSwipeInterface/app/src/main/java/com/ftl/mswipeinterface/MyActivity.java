package com.ftl.mswipeinterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.ftl.paymentgateway.DeviceState;
import com.ftl.paymentgateway.IPaymentGateway;
import com.ftl.paymentgateway.MSwipeGateway;
import com.ftl.paymentgateway.PaymentGateway;
import com.ftl.paymentgateway.PaymentGatewayFactory;
import com.ftl.paymentgateway.PaymentRequest;

import org.json.JSONArray;

import java.util.ArrayList;

public class MyActivity extends Activity {
    public static final String TAG = MyActivity.class.getSimpleName();
    public final static String SELECTED_PAYMENT_GATEWAY = "selected_payment_gateway";
    public final static String MERCHANT_ID = "merchant_id";
    public final static String TERMINAL_ID = "terminal_id";
    public final static String BLUETOOTH_NAME = "blu_name";
    public final static String BLUETOOTH_ADDRESS = "blu_addrs";
    public final static String IP_ADDRESS = "IP_Address";
    public final static String PORT_NUMBER = "Port_Number";

    public final static String SECURITY_TOKEN = "SECURITY_TOKEN";
    public final static String STORE_POS_CODE = "STORE_POS_CODE";
    public final static String IMEI = "IMEI";
    public final static String USER_ID = "USER_ID";
    public final static String TRANSACTION_NUMBER = "Transaction_Number";

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_ONGO_GATEWAY = 1;
    public WebView myWebView;
    public IPaymentGateway paymentGateway;
    public SharedPreferences sharedPref;
    public SharedPreferences.Editor editor;
    private MyLogger logger;
    private WifiManager.WifiLock wifiLock;
    final Context mapp = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // paymentGateway = new PaymentGatewayFactory().paymentGateway(PaymentGateway.Ongo, this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

                // Initializing the logger
        logger = MyLogger.getInstance();

        // Initializing the wifiLock
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "LockTag");

        // Making the webview debuggable from Chrome
        WebView.setWebContentsDebuggingEnabled(true);

        // Initializing the webview
        logger.debug(TAG, "Initializing the webview", this);
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.getSettings().setJavaScriptEnabled(true);
        // Enabling these so that the html5 storage works
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.getSettings().setDatabaseEnabled(true);
        myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        myWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        myWebView.loadUrl("file:///android_asset/index.html");
        myWebView.addJavascriptInterface(new WebAppInterface(this, myWebView), "Android");
        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        // Capturing console logs and logging them from the android app
        myWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                logger.debug(TAG, cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId(), MyActivity.this);
                return true;
            }


            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
                new AlertDialog.Builder(mapp)
                        .setTitle("")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                }).setCancelable(false).create().show();

                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final android.webkit.JsResult result) {
                new AlertDialog.Builder(mapp)
                        .setTitle("")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                }).setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        }).create().show();
                return true;
            }
        });

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String selectedGateway = sharedPref.getString(SELECTED_PAYMENT_GATEWAY, "");
        if (selectedGateway.equals("Ongo")) {
            if (sharedPref.getString(MERCHANT_ID, "") != null && sharedPref.getString(MERCHANT_ID, "") != "") {
                PaymentRequest request = new PaymentRequest();
                request.Bluetooth_id = sharedPref.getString(BLUETOOTH_ADDRESS, "");
                request.Bluetooth_name = sharedPref.getString(BLUETOOTH_NAME, "");
                request.Merchant_id = sharedPref.getString(MERCHANT_ID, "");
                request.Terminal_id = sharedPref.getString(TERMINAL_ID, "");

                paymentGateway = null;
                PaymentGateway selectedPayment = PaymentGateway.valueOf(selectedGateway);
                paymentGateway = new PaymentGatewayFactory().paymentGateway(request, selectedPayment, this);
                //String sessionkey =  Getsessionkey(request);
                sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                editor = sharedPref.edit();
                editor.putString("sessionkey","");
                editor.commit();
            }
        }
        else if (selectedGateway.equals("SingaporePayment")) {
            if (sharedPref.getString(IP_ADDRESS, "") != null && sharedPref.getString(IP_ADDRESS, "") != "") {
                PaymentRequest request = new PaymentRequest();
                request.Ip_Address = sharedPref.getString(IP_ADDRESS, "");
                request.Port_Number = sharedPref.getString(PORT_NUMBER, "");

                paymentGateway = null;
                PaymentGateway selectedPayment = PaymentGateway.valueOf(selectedGateway);
                paymentGateway = new PaymentGatewayFactory().paymentGateway(request, selectedPayment, this);
            }
        }
        else if (selectedGateway.equals("MSwipeInterface")) {
            paymentGateway = null;
            PaymentGateway selectedPayment = PaymentGateway.valueOf(selectedGateway);
            paymentGateway = new PaymentGatewayFactory().paymentGateway(null, selectedPayment, this);
        }
        else if (selectedGateway.equals("PineLabsPayment")) {
            if (sharedPref.getString(MERCHANT_ID, "") != null && sharedPref.getString(SECURITY_TOKEN, "") != ""
                    && sharedPref.getString(STORE_POS_CODE, "") != ""&& sharedPref.getString(TRANSACTION_NUMBER, "") != ""
                    && sharedPref.getString(IMEI, "") != ""&& sharedPref.getString(USER_ID, "") != "") {
                PaymentRequest request = new PaymentRequest();
                request.Merchant_id = sharedPref.getString(MERCHANT_ID, "");
                request.Security_Token = sharedPref.getString(SECURITY_TOKEN, "");
                request.Store_Pos_Code = sharedPref.getString(STORE_POS_CODE, "");
                request.IMEI = sharedPref.getString(IMEI, "");
                request.User_ID = sharedPref.getString(USER_ID, "");
                request.Transaction_Number = sharedPref.getString(TRANSACTION_NUMBER, "");

                paymentGateway = null;
                PaymentGateway selectedPayment = PaymentGateway.valueOf(selectedGateway);
                paymentGateway = new PaymentGatewayFactory().paymentGateway(request, selectedPayment, this);

            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        // acquiring the wifilock
        wifiLock.acquire();
        super.onStart();
    }

    @Override
    public void onDestroy() {
        disconnectDevice();
        // releasing the wifilock
        wifiLock.release();
        logger.clearLog(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        //TODO: verify this change, else revert
        if (keycode == KeyEvent.KEYCODE_BACK) {
            if (paymentGateway != null) {
                paymentGateway.RevertTransaction();
            }
        }
        return super.onKeyDown(keycode, event);
    }

    // The callback when an exterior activity fulfils its result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            logger.debug(TAG, "RequestCode" + requestCode + " resultCode" + resultCode + "Data from Mpos" + data,this);
            switch (requestCode) {
                case REQUEST_ENABLE_BT:
                    if (resultCode == RESULT_OK) {
                        logger.debug(TAG, "Successfully made a bluetooth connection", this);
                        connectToDevice(false);
                    }
                case REQUEST_ONGO_GATEWAY:
                    if (resultCode == 2 ) {
                        String resResult = data.getExtras().getString("Result");
                        logger.debug(TAG, "Failure transaction result" + resResult, this);
                        showCardFailureScreen(resResult);
                    } else if (resultCode == 0 ) {
                        String resResult = data.getExtras().getString("Result");
                        if (resResult.contains("Approved")) {
                            logger.debug(TAG, "Swipe card Transaction Successful" + resResult, this);
                            showCardSuccessScreen("", "Swipe card Transaction");
                        } else {
                            logger.debug(TAG, "Failure transaction result" + resResult, this);
                            showCardFailureScreen(resResult);
                        }
                    } else if (resultCode == -1) {
                        //Toast.makeText(mapp, "got approved text from Ongo ResultCode -1", Toast.LENGTH_LONG).show();
                        showCardSuccessScreen("swipe", "Transaction");
                    } else if (resultCode == 3) {
                        String resResult = data.getExtras().getString("Result");
                        logger.debug(TAG, "Failure transaction result" + resResult, this);
                        showCardFailureScreen(resResult);
                    }
            }
        }
        catch (Exception e) {
                logger.info(MyActivity.TAG, "Transaction Result Error " + resultCode, this);
        }
    }

    // Helper functions
    // This is called from the UI when user initiates a card transaction
    public void checkCard(String total_money) {
        logger.debug(TAG, "total_money"+total_money, this);
        if (paymentGateway != null) {
            sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            String selectedGateway = sharedPref.getString(SELECTED_PAYMENT_GATEWAY, "");
            if (selectedGateway.equals("Ongo")) {
            String sessionkey = sharedPref.getString("sessionkey","");
            logger.debug(TAG, "Shared Preference session Key "+sessionkey, this);
            if(sessionkey == "" || sessionkey == null || sessionkey=="No Response From Server") {
                    if (sharedPref.getString(MERCHANT_ID, "") != null && sharedPref.getString(MERCHANT_ID, "") != "") {
                        PaymentRequest request = new PaymentRequest();
                        request.Bluetooth_id = sharedPref.getString(BLUETOOTH_ADDRESS, "");
                        request.Bluetooth_name = sharedPref.getString(BLUETOOTH_NAME, "");
                        request.Merchant_id = sharedPref.getString(MERCHANT_ID, "");
                        request.Terminal_id = sharedPref.getString(TERMINAL_ID, "");

                        paymentGateway = null;
                        PaymentGateway selectedPayment = PaymentGateway.valueOf(selectedGateway);
                        paymentGateway = new PaymentGatewayFactory().paymentGateway(request, selectedPayment, this);
                        sessionkey = Getsessionkey(request);
                        logger.debug(TAG,"NEW SESSION KEY FIRST: " + sessionkey,this);
                        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                        editor = sharedPref.edit();
                        editor.putString("sessionkey",sessionkey);
                        editor.commit();
                    }
                }
                logger.debug(TAG, "session Key "+sessionkey, this);
                paymentGateway.Pay(total_money,sessionkey);
            }
            else {
                paymentGateway.Pay(total_money,"");
            }
        }
    }

    // This cancels the checkCard mode and brings the device to ready state
    public void cancelCheckCard() {
        if (paymentGateway != null) {
            paymentGateway.CancelCheckCard();
        }
    }

    public void showLogOutput() {
        final String log_content = logger.ReadFromFile(this);
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                myWebView.evaluateJavascript("$('#logs_area').text('" + log_content + "')", null);
            }
        });
    }

    public void setSelectedPaymentGateway(String selectedPaymentGateway, PaymentRequest paymentRequest) {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putString(this.SELECTED_PAYMENT_GATEWAY, selectedPaymentGateway).commit();

        if (paymentRequest != null) {
            editor.putString(this.MERCHANT_ID, paymentRequest.Merchant_id).commit();
            editor.putString(this.TERMINAL_ID, paymentRequest.Terminal_id).commit();
            editor.putString(this.BLUETOOTH_ADDRESS, paymentRequest.Bluetooth_id).commit();
            editor.putString(this.BLUETOOTH_NAME, paymentRequest.Bluetooth_name).commit();
            editor.putString(this.IP_ADDRESS, paymentRequest.Ip_Address).commit();
            editor.putString(this.PORT_NUMBER, paymentRequest.Port_Number).commit();
            editor.putString(this.SECURITY_TOKEN, paymentRequest.Security_Token).commit();
            editor.putString(this.STORE_POS_CODE, paymentRequest.Store_Pos_Code).commit();
            editor.putString(this.IMEI, paymentRequest.IMEI).commit();
            editor.putString(this.USER_ID, paymentRequest.User_ID).commit();
            editor.putString(this.TRANSACTION_NUMBER, paymentRequest.Transaction_Number).commit();
        }

        paymentGateway = null;
        PaymentGateway selectedPayment = PaymentGateway.valueOf(selectedPaymentGateway);
        paymentGateway = new PaymentGatewayFactory().paymentGateway(paymentRequest, selectedPayment, this);
    }

    public void setPaymentGatewayTypes() {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String selectedGateway = sharedPref.getString(SELECTED_PAYMENT_GATEWAY, "");

        PaymentGateway[] paymentTypes = PaymentGateway.values();
        ArrayList paymentTypeList = new ArrayList();

        for (int i = 0; i < paymentTypes.length; i++) {
            paymentTypeList.add(paymentTypes[i].name());
        }
        paymentTypeList.add(selectedGateway);

        final String paymentTypeJson = (new JSONArray(paymentTypeList)).toString();
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                myWebView.evaluateJavascript("setPaymentValues('" + paymentTypeJson + "')", null);
            }
        });
    }

    // Reading the log file and mailing all the contents through the
    // installed email client
    public void mailLogs() {
        String log_content = logger.ReadFromFile(this);
        log_content = log_content.replaceAll("\\\\n", "\n");
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"atchayamindia@atchayam.in"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Order app logs");
        i.putExtra(Intent.EXTRA_TEXT, log_content);
        try {
            logger.info(TAG, "Sending mail..", this);
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            logger.error(TAG, "There are no mail clients installed", this);
        }
        logger.info(TAG, "Successfully sent mail", this);
    }

    // This is called when a successful card transaction is made
    public void showCardSuccessScreen(String cardNo, final String cardholderName) {
        paymentGateway.ShowCardSuccessScreen(cardNo, cardholderName);
    }

    // This is called when some error occurs in the card transaction
    public void showCardFailureScreen(String displayMsg) {
        paymentGateway.ShowCardFailureScreen(displayMsg);
    }

    // This is called to display the bank summary for that day.
    public void showBankSummary() {
        paymentGateway.ShowBankSummary();
    }

    public boolean connectToDevice(boolean duringTransaction) {

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String selectedGateway = sharedPref.getString(SELECTED_PAYMENT_GATEWAY, "");
        if(selectedGateway=="MswipeInterface") {
            if (MSwipeGateway.deviceState != DeviceState.CONNECTING
                    && !MSwipeGateway.wisePadController.isDevicePresent()) {
               return paymentGateway.connectToDevice(duringTransaction);
            }
        }else {
            return paymentGateway.connectToDevice(duringTransaction);
        }
        return false;
    }

    public void disconnectDevice() {
        // stopping the bluetooth connection
        if (paymentGateway != null) {
            paymentGateway.DisconnectDevice();
        }
    }

    public String Getsessionkey(PaymentRequest data)
    {
        String sessionkey = paymentGateway.Getsessionkey(data);
        return sessionkey;
    }
}
