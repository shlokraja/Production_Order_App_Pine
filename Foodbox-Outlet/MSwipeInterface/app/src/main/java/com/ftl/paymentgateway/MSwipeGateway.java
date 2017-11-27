package com.ftl.paymentgateway;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.ftl.mswipeinterface.Constants;
import com.ftl.mswipeinterface.Impl_MswipeWisePadDeviceListener;
import com.ftl.mswipeinterface.MyActivity;
import com.ftl.mswipeinterface.MyLogger;
import com.mswipetech.wisepad.sdk.MswipeWisepadController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

/**
 * Created by rajamanickam.r on 6/28/2016.
 */
public class MSwipeGateway implements IPaymentGateway {
    public static Context myContext;
    public static final String TAG = MyActivity.class.getSimpleName();
    public static MswipeWisepadController wisePadController;
    public static final String BLUETOOTH_MAC_ID = "bluetooth_mac_id";
    public static TransactionType transactionType = TransactionType.EMV;
    public static DeviceState deviceState = DeviceState.NOT_CONNECTED;
    public static BluetoothConnectionState bluetoothConnectionState = BluetoothConnectionState.NO_MATCHED_DEVICE;
    public ArrayList<BluetoothDevice> pairedDevicesFound = new ArrayList<BluetoothDevice>();
    private static final String[] MONTHS = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    public String bankSummaryDate="";
    private static final int REQUEST_ENABLE_BT = 0;
    private Impl_MswipeWisePadDeviceListener listener;
    private static MyLogger logger;
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";
    public static final String WISEPAD_REFERENCE = "wisepad_reference";
    public static final String WISEPAD_SESSION_TOKEN = "wisepad_session_token";
    public static final String WISEPAD_MOBILE_NO = "wisepad_mobile";
    public static final String INSPIRENETZ_DIGEST_AUTH = "digest_auth";
    public static final String INSPIRENETZ_USERNAME = "inspire_user";
    public static final String INSPIRENETZ_PASSWORD = "inspire_password";
    public static final String INSPIRENETZ_HTTP_URL = "inspire_url";
    public static boolean test_mode = false;
    public static String amount = "";

    public MSwipeGateway(MyActivity myActivity) {
        myContext = myActivity;
        logger = MyLogger.getInstance();

        // Initializing listener
        listener = new Impl_MswipeWisePadDeviceListener(myActivity);

        // Initializing wisepad controller
        wisePadController = new MswipeWisepadController(myActivity, MswipeWisepadController.GATEWAYENV.PRODUCTION);

        // Initiating the device listener
        wisePadController.initMswipeWisePadDevice(listener);
    }

    @Override
    public String Getsessionkey(PaymentRequest data){
        String sessionkey = "";
        return sessionkey;
    }

    @Override
    public void Pay(String payment_amount,String sessionkey) {

        if (deviceState == DeviceState.CONNECTED) {
            if (wisePadController.isDevicePresent()) {
                logger.info(TAG, "Going ahead with getting the swipe info", myContext);
                wisePadController.checkCard();
            } else {
                logger.error(TAG, "No paired WisePad device found. Pair the device and try again.", myContext);
            }
        } else if (deviceState == DeviceState.NOT_CONNECTED) {
            connectToDevice(true);
        } else {
            logger.info(TAG, "Device is connecting, please try after some time", myContext);
        }
    }

    public boolean connectToDevice(boolean duringTransaction) {
        pairedDevicesFound.clear();
        logger.info(TAG, "Connecting to device", myContext);
        if (BluetoothAdapter.getDefaultAdapter() == null)
            return false;
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            logger.info(TAG, "Switching bluetooth on", myContext);
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((MyActivity)myContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            logger.info(TAG, "Successfully Switched on the Bluetooth", myContext);
            return false;
        } else {
            logger.info(TAG, " Bluetooth is on ", myContext);
            // Getting the stored bluetooth id first
            SharedPreferences sharedPref =  ((MyActivity)myContext).getPreferences(Context.MODE_PRIVATE);
            String bluetooth_mac_id = sharedPref.getString(BLUETOOTH_MAC_ID, "");
            // Iterating and filtering through the list of paired devices
            Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    //TODO: [Bad shlok code].This logic sucks. Need to improve it during testing
                    if (device.getName().toLowerCase().startsWith("wisepad") ||
                            device.getName().toLowerCase().startsWith("wp") ||
                            device.getName().toLowerCase().startsWith("1084")) {
                        if (bluetooth_mac_id.equals(device.getAddress())) {
                            pairedDevicesFound.clear();
                            pairedDevicesFound.add(device);
                            break;
                        } else {
                            pairedDevicesFound.add(device);
                        }
                    }
                }
            }

            // TODO: [Bad shlok code]This entire business is irrelevant, we can make the connection in previous loop itself
            // Depending on the no. of matched devices, returning the result
            if (pairedDevicesFound.size() > 1) {
                bluetoothConnectionState = BluetoothConnectionState.MULTIPLE_MATCHED_DEVICES;
                ((MyActivity)myContext).myWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        ((MyActivity)myContext).myWebView.evaluateJavascript("$('#settingsModal #status_text').empty(); $('#settingsModal #status_text').text('Sorry, multiple matched devices found.');", null);
                    }
                });
                logger.error(TAG, "Multiple matched devices found", myContext);
                return false;
            } else if (pairedDevicesFound.size() == 1) {
                deviceState = DeviceState.CONNECTING;
                bluetoothConnectionState = BluetoothConnectionState.SINGLE_MATCHED_DEVICE;
                String displayMsg = "Exact match to deviceID. Starting connection";
                ((MyActivity)myContext).myWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        //Copying the string again otherwise have to declare final, which is not a good practice
                        ((MyActivity)myContext).myWebView.evaluateJavascript("$('#settingsModal #status_text').empty(); $('#settingsModal #status_text').text('Exact match to device id. Starting connection.');", null);
                    }
                });
                logger.info(TAG, displayMsg, myContext);
                wisePadController.startBTv2(pairedDevicesFound.get(0));
                return true;
            } else {
                ((MyActivity)myContext).myWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        ((MyActivity)myContext).myWebView.evaluateJavascript("$('#settingsModal #status_text').empty(); $('#settingsModal #status_text').text('Sorry, not matched devices found.');", null);
                    }
                });
                logger.error(TAG, "No matched devices found", myContext);
                bluetoothConnectionState = BluetoothConnectionState.NO_MATCHED_DEVICE;
                if (duringTransaction) {
                    final String msgToShow = "Could not connect to Mswipe device";
                    ((MyActivity)myContext). myWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            ((MyActivity)myContext).myWebView.evaluateJavascript("showFailureScreen('"+ msgToShow +"')", null);
                        }
                    });
                }
                return false;
            }
        }
    }

    @Override
    public void DisconnectDevice() {
        wisePadController.stopBTv2();
    }

    @Override
    public void RevertTransaction() {
        if (transactionType == TransactionType.EMV) {
            // Cancelling the transaction
            wisePadController.sendOnlineProcessResult(null);
        }
    }

    @Override
    public void CancelCheckCard() {
        wisePadController.cancelCheckCard();
    }

    @Override
    public void ShowCardSuccessScreen(String cardNo, final String cardholderName) {
        final String last4Digits;
        int ilen = cardNo.length();
        if (ilen >= 4)
            last4Digits = cardNo.substring(ilen - 4, ilen);
        else
            last4Digits = cardNo;
        // disconnecting device to flush cache
        DisconnectDevice();
        ((MyActivity)myContext).myWebView.post(new Runnable() {
            @Override
            public void run() {
                ((MyActivity)myContext).myWebView.evaluateJavascript("showSuccessScreen('"+last4Digits+"','"+cardholderName+"')", null);
            }
        });

    }

    @Override
    public void ShowCardFailureScreen(String displayMsg) {
        final String msgToShow = displayMsg;
        // disconnecting device to flush cache
        DisconnectDevice();
        ((MyActivity)myContext).myWebView.post(new Runnable() {
            @Override
            public void run() {
                ((MyActivity)myContext).myWebView.evaluateJavascript("showFailureScreen('" + msgToShow + "')", null);
            }
        });
    }

    @Override
    public void ShowBankSummary() {
        SharedPreferences sharedPref =  ((MyActivity)myContext).getPreferences(Context.MODE_PRIVATE);
        String wisepad_reference = sharedPref.getString(MSwipeGateway.WISEPAD_REFERENCE, "");
        String wisepad_session_token = sharedPref.getString(MSwipeGateway.WISEPAD_SESSION_TOKEN, "");
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        bankSummaryDate =  day + "/" + MONTHS[month] + "/" + year;
        String mSelectedDate = year + "-" + (month + 1) + "-" + day;
        wisePadController.getBankSummary(BankSummaryHandler,
                wisepad_reference,
                wisepad_session_token,
                mSelectedDate);
    }

    // TODO: make this class static
    private Handler BankSummaryHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Bundle bundle = msg.getData();
            String responseMsg = bundle.getString("response");

            String[][] strTags = new String[][]{{"status", ""}, {"ErrMsg", ""}, {"BankSaleAmount", ""}, {"BankSaleCount", ""}};

            try
            {
                Constants.parseXml(responseMsg, strTags);
                String status = strTags[0][1];
                if (status.equalsIgnoreCase("False")) {
                    logger.error(TAG, "The bank summary was not processed successfully", myContext);
                    String ErrMsg = strTags[1][1];
                    ((MyActivity)myContext).myWebView.evaluateJavascript("$('#settingsModal #status_text').empty(); $('#settingsModal #status_text').text('"+ ErrMsg +"');", null);
                }else{
                    logger.debug(TAG, "The bank summary was processed successfully", myContext);
                    String saleAmount = strTags[2][1];
                    String saleCount = strTags[3][1];
                    ((MyActivity)myContext).myWebView.evaluateJavascript("showBankSummary("+saleAmount+","+saleCount+","+bankSummaryDate+")",
                            null);
                }
            }
            catch (Exception ex) {
                logger.error(TAG, "Exception in bankSummary - " + ex.toString(), myContext);
            }
        }
    };


    // TODO: Make this class static
    public static Handler loginHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg) {

            Bundle bundle = msg.getData();
            String responseMsg = bundle.getString("response");

            String[][] strTags = new String[][]{{"status", ""}, {"ErrMsg", ""},
                    {"IS_Password_Changed", ""}, {"First_Name", ""},
                    {"Reference_Id", ""}, {"Session_Tokeniser", ""},
                    {"Currency_Code", ""}};
            try
            {
                Constants.parseXml(responseMsg, strTags);
                String status = strTags[0][1];
                if (status.equalsIgnoreCase("false")) {
                    String displayMsg = "Authentication Failure with mswipe";
                    ((MyActivity)myContext).myWebView.evaluateJavascript("$('#settingsModal #status_text').empty(); $('#settingsModal #status_text').text('"+ displayMsg + "');", null);
                    logger.error(TAG, displayMsg, myContext);
                } else if (status.equalsIgnoreCase("true")){
                     String displayMsg = "mSwipe login successful !";
                    ((MyActivity)myContext).myWebView.evaluateJavascript("$('#settingsModal #status_text').empty(); $('#settingsModal #status_text').text('"+ displayMsg + "');", null);
                    logger.info(TAG, displayMsg, myContext);
                    String FirstName =  strTags[3][1];

                    String reference_Id =  strTags[4][1];
                    String session_Token =  strTags[5][1];
                    String currency_code = strTags[6][1];
                    logger.info(TAG, "Currency code returned is - " + currency_code, myContext);

                    // Putting the reference and session_token in a preference
                    SharedPreferences sharedPref = ((MyActivity)myContext).getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(WISEPAD_REFERENCE, reference_Id).commit();
                    editor.putString(WISEPAD_SESSION_TOKEN, session_Token).commit();
                }else{
                    String displayMsg = "Unknown status returned from mswipe - " + status;
                    logger.error(TAG, displayMsg, myContext);
                    ((MyActivity)myContext).myWebView.evaluateJavascript("$('#settingsModal #status_text').empty(); $('#settingsModal #status_text').text('"+ displayMsg + "');", null);
                }
            }
            catch (Exception ex) {
                logger.error(TAG, ex.toString(), myContext);
            }
        }
    };
}
