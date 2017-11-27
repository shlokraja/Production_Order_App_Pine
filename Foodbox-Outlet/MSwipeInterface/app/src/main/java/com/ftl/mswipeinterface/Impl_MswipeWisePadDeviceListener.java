package com.ftl.mswipeinterface;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ftl.paymentgateway.DeviceState;
import com.ftl.paymentgateway.MSwipeGateway;
import com.mswipetech.wisepad.sdk.MswipeWisePadDeviceListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.ftl.paymentgateway.TransactionType;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class Impl_MswipeWisePadDeviceListener implements MswipeWisePadDeviceListener {
    private static final String TAG = Impl_MswipeWisePadDeviceListener.class.getSimpleName();
    // This is used by wisepad to set the amount
    // TODO: Wisepad login result returns the currency_code. Better to set that from there
    private  static final String CURRENCY_CODE = "356";
    private  static final String CHARGE_CARD_START = "8013";
    private Context mContext;
    private MyLogger logger;

    // Fields for card details
    String mSno = "";
    String mCreditCardNo = "";
    String mCardHolderName = "";
    String mExpiryDate = "";
    String mPaddingInfo = "";
    String mEncTrackData = "";
    String mEPB = "";
    String mEPBKsn = "";
    String mAmexSecurityCode = "";
    String mAppIdentifier = "";
    String mTVR = "";
    String mTSI = "";
    String mApplicationName = "";
    String mCertif = "";
    String mEmvCardExpdate = "";
    String mSwitchCardType = "";
    String mStandId = "";
    String mF055tag = "";
    String mAuthCode = "";
    String mRRNo = "";
    String mDate = "";
    String mMid = "";
    String mTid = "";
    String mBatchNo = "";
    String mIssuerAuthCode = "";
    String mTlvDataCoAndC3 = "";
    String mswipeStatus = "";

    boolean isPinCanceled = false;
    // Constructor to store the mContext
    public  Impl_MswipeWisePadDeviceListener(Context c) {
        mContext = c;
        logger = MyLogger.getInstance();
    }

    @Override
    public void onWaitingForCard() {
        logger.debug(TAG, "Waiting for the user to swipe ...", mContext);
    }

    @Override
    public void onRequestInsertCard() {
        // XXX: This is a bit ambiguous. API doc says this happens when wisepad has asked to insert card.
        // Not when it detects card not inserted.
        logger.error(TAG, "Card is not inserted, please insert/swipe card and click button again.", mContext);
    }

    @Override
    public void onReturnCheckCardResult(CheckCardResult checkCardResult, Hashtable<String, String> decodeData) {
        logger.debug(TAG, "onReturnCheckCardResult called", mContext);
        // Clearing off the card details fields
        resetCardDetails();

        if (checkCardResult == CheckCardResult.NONE) {
            logger.error(TAG, "No result from card swipe", mContext);
            ((MyActivity) mContext).showCardFailureScreen("No result from card swipe");
        } else if (checkCardResult == CheckCardResult.ICC) {
            logger.debug(TAG, "Starting an emv transaction", mContext);
            MSwipeGateway.transactionType = com.ftl.paymentgateway.TransactionType.EMV;
            MSwipeGateway.wisePadController.startEmv();
        } else if (checkCardResult == CheckCardResult.NOT_ICC) {
            logger.error(TAG, "Card is not an emv card", mContext);
            ((MyActivity) mContext).showCardFailureScreen("Card is not an emv card");
        } else if (checkCardResult == CheckCardResult.BAD_SWIPE) {
            logger.error(TAG, "Bad swipe detected. Please try again", mContext);
            ((MyActivity) mContext).showCardFailureScreen("Bad swipe detected. Please try again");
        } else if (checkCardResult == CheckCardResult.MCR) {
            logger.info(TAG, "MCR card detected. Processing data", mContext);
            processMagCardData(checkCardResult, decodeData);
        } else if (checkCardResult == CheckCardResult.SERVICECODE_FAIL_USE_CHIPCARD) {
            logger.error(TAG, "Please use a chip card", mContext);
            ((MyActivity) mContext).showCardFailureScreen("Please use a chip card");
        } else if (checkCardResult == CheckCardResult.MCR_AMEXCARD) {
            logger.info(TAG, "MCR Amex card detected. Processing data", mContext);
            processMagCardData(checkCardResult, decodeData);
        } else if (checkCardResult == CheckCardResult.NO_RESPONSE) {
            logger.error(TAG, "No response from device", mContext);
            ((MyActivity) mContext).showCardFailureScreen("No response from device");
        } else if (checkCardResult == CheckCardResult.TRACK2_ONLY) {
            processMagCardData(checkCardResult, decodeData);
        }
    }

    @Override
    public void onReturnStartEmvResult(StartEmvResult startEmvResult, String ksn) {
        logger.debug(TAG, "OnReturnStartEMVResult called", mContext);
        if (startEmvResult == StartEmvResult.SUCCESS) {
            logger.debug(TAG, "Successfully returned emv result", mContext);
        } else {
            logger.error(TAG, "Error received on ReturnStartEmvResult. Please insert/swipe card and press button again.", mContext);
        }
    }

    @Override
    public void onRequestSetAmount() {
        logger.debug(TAG, "onRequestSetAmount called", mContext);
        String totalAmount = MSwipeGateway.amount;
        logger.debug(TAG, "Setting amount to " +totalAmount +" Rs.", mContext);
        MSwipeGateway.wisePadController.setAmount(totalAmount, CURRENCY_CODE);
    }

    @Override
    public void onReturnAmountConfirmResult(boolean amount_confirmed) {
        logger.debug(TAG, "onReturnAmountConfirmResult called", mContext);
        if (amount_confirmed) {
            logger.debug(TAG, "Amount was confirmed", mContext);
        } else {
            logger.debug(TAG, "Amount was not confirmed", mContext);
            //TODO: Need to cancel the transaction here
        }
    }

    @Override
    public void onRequestSelectApplication(ArrayList<String> appList) {
        logger.debug(TAG, "onRequestSelectApplication called", mContext);
        logger.debug(TAG, "List of applications received- " + appList.toString(), mContext);
        if (appList.size() == 1) {
            logger.debug(TAG, "Selecting only a single application", mContext);
            MSwipeGateway.wisePadController.selectApplication(0);
        } else {
            logger.debug(TAG, "There are more than one application. Showing a dialog to choose one", mContext);
            final Dialog dialog = getAppSelectDialog(mContext, "Select an app from the list");
            String[] appNameList = new String[appList.size()];
            for (int i = 0; i < appNameList.length; ++i) {
                appNameList[i] = appList.get(i);
            }

            ListView appListView = (ListView) dialog.findViewById(R.id.creditsale_LST_applications);
            appListView.setAdapter(new AppSelectAdapter(mContext, appList));
            appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MSwipeGateway.wisePadController.selectApplication(position);
                }
            });

            dialog.findViewById(R.id.bmessageDialogNo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MSwipeGateway.wisePadController.cancelSelectApplication();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onRequestPinEntry(PinEntry pinEntry) {
        logger.debug(TAG, "onRequestPinEntry called", mContext);
        logger.debug(TAG, "Please enter the PIN.", mContext);
    }

    @Override
    public void onReturnPinEntryResult(PinEntryResult pinEntryResult, String epb, String ksn) {
        logger.debug(TAG, "onReturnPinEntryResult called", mContext);
        if (pinEntryResult == PinEntryResult.ENTERED) {
            logger.info(TAG, "Correct PIN was entered", mContext);
        } else if (pinEntryResult == PinEntryResult.BYPASS) {
            logger.info(TAG, "PIN was bypassed", mContext);
        } else if (pinEntryResult == PinEntryResult.CANCEL) {
            logger.error(TAG, "This step was cancelled", mContext);
            isPinCanceled = true;
        } else if (pinEntryResult == PinEntryResult.TIMEOUT) {
            logger.error(TAG, "A timeout occurred while entering the PIN", mContext);
            isPinCanceled = true;
        } else if (pinEntryResult == PinEntryResult.KEY_ERROR ||
                    pinEntryResult == PinEntryResult.INCORRECT_PIN ||
                    pinEntryResult == PinEntryResult.WRONG_PIN_LENGTH) {
            logger.error(TAG, "Bad PIN was entered", mContext);
            isPinCanceled = true;
        } else if (pinEntryResult == PinEntryResult.NO_PIN) {
            logger.error(TAG, "No PIN was entered", mContext);
            isPinCanceled = true;
        }

    }

    @Override
    public void onRequestTerminalTime() {

    }

    @Override
    public void onRequestCheckServerConnectivity() {

    }

    @Override
    public void onRequestFinalConfirm() {
        logger.debug(TAG, "onRequestFinalConfirm called", mContext);
    }

    @Override
    public void onRequestOnlineProcess(HashMap<String, String> tlv) {
        logger.debug(TAG, "onRequestOnlineProcess called", mContext);
        if (MSwipeGateway.test_mode) {
            logger.debug(TAG, "Test mode is enabled. Directly going to success screen", mContext);
            ((MyActivity) mContext).showCardSuccessScreen(mCreditCardNo, mCardHolderName);
            return;
        }
        storeTlvData(tlv);
        // XXX: Agniva - Might be needed later
        //String mTlvDataCoAndC3 = tlv.get("TlvDataCoAndC3");
        SharedPreferences sharedPref = ((MyActivity) mContext).getPreferences(Context.MODE_PRIVATE);
        String wisepad_reference = sharedPref.getString(MSwipeGateway.WISEPAD_REFERENCE, "");
        String wisepad_session_token = sharedPref.getString(MSwipeGateway.WISEPAD_SESSION_TOKEN, "");
        String wisepad_mobile_no = sharedPref.getString(MSwipeGateway.WISEPAD_MOBILE_NO, "");

        // Making the call to get the transaction done
        MSwipeGateway.wisePadController.CreditSale_EMV(CreditSaleHandler,
                wisepad_reference,
                wisepad_session_token,
                "",
                MSwipeGateway.amount,
                wisepad_mobile_no,
                "",
                "",
                mTVR,
                mTSI);
    }

    @Override
    public void onRequestReferProcess(String s) {

    }

    @Override
    public void onRequestAdviceProcess(String s) {

    }

    @Override
    public void onReturnReversalData(String s) {
        logger.debug(TAG, "onReturnReversalData called. This means payment was reversed.", mContext);
    }

    @Override
    public void onReturnBatchData(HashMap<String, String> tlv) {
        logger.debug(TAG, "onReturnBatchData called. This means payment was successful.", mContext);
        //TODO: [weird SHLOK code] Need to check why only these params
        mCertif = tlv.get("Certif");
        mTVR = tlv.get("TVR");
        mTSI = tlv.get("TSI");
    }

    @Override
    public void onReturnTransactionResult(TransactionResult transactionResult, String offlineDeclineTag) {
        logger.debug(TAG, "onReturnTransactionResult called", mContext);

        //Kumaresan on 02-05-2016
        // for offline declined status, we force the application to treat that as approved transaction
        // and we make the application to proceed further
        if (transactionResult == TransactionResult.APPROVED || mswipeStatus.equalsIgnoreCase("true")){
            logger.info(TAG, "Transaction approved. You may now remove the card", mContext);
            ((MyActivity) mContext).showCardSuccessScreen(mCreditCardNo, mCardHolderName);
        } else if (transactionResult == TransactionResult.AUTO_REVERSAL_TRX) {
            logger.info(TAG, "Transaction needs to be reversed", mContext);
            // Getting the date formatted
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
            Date date = null;
            try {
                date = format.parse(mDate);
            } catch (ParseException e) {
                // XXX: Agniva- Weird Java. Checked exceptions MUST be caught.
                logger.error(TAG, e.toString(), mContext);
            }
            SimpleDateFormat formatNew = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = formatNew.format(date);

            // Getting the last 4 digits of the card
            String last4Digits = "";
            int ilen = mCreditCardNo.length();
            if (ilen >= 4)
                last4Digits = mCreditCardNo.substring(ilen - 4, ilen);
            else
                last4Digits = mCreditCardNo;

            SharedPreferences sharedPref = ((MyActivity) mContext).getPreferences(Context.MODE_PRIVATE);
            String wisepad_reference = sharedPref.getString(MSwipeGateway.WISEPAD_REFERENCE, "");
            String wisepad_session_token = sharedPref.getString(MSwipeGateway.WISEPAD_SESSION_TOKEN, "");
            // Reversing the transaction
            MSwipeGateway.wisePadController.AutoReversalCardSaleTrx(voidSaleReversalHandler,
                    wisepad_reference,
                    wisepad_session_token,
                    formattedDate,
                    MSwipeGateway.amount,
                    last4Digits,
                    mStandId,
                    mF055tag,
                    offlineDeclineTag);
        } else if (transactionResult == TransactionResult.DECLINED) {
            String displayMsg = "Transaction was declined";
            logger.error(TAG, displayMsg, mContext);
            ((MyActivity) mContext).showCardFailureScreen(displayMsg);
        } else if (transactionResult == TransactionResult.DEVICE_ERROR) {
            String displayMsg = "Some device error occurred during the transaction";
            logger.error(TAG, displayMsg, mContext);
            ((MyActivity) mContext).showCardFailureScreen(displayMsg);
        } else {
            String displayMsg = "Error occured during transaction";
            logger.error(TAG, displayMsg, mContext);
            ((MyActivity) mContext).showCardFailureScreen(displayMsg);
        }
    }

    @Override
    public void onReturnTransactionLog(String s) {

    }

    @Override
    public void onRequestDisplayText(DisplayText displayText) {

    }

    @Override
    public void onRequestClearDisplay() {

    }

    @Override
    public void onReturnDeviceInfo(Hashtable<String, String> hashtable) {

    }

    @Override
    public void onBatteryLow(BatteryStatus batteryStatus) {

    }

    @Override
    public void onBTv2DeviceNotFound() {

    }

    @Override
    public void onBTv2Detected() {

    }

    @Override
    public  void onBTv2Connected(BluetoothDevice bluetoothDevice) {
        logger.debug(TAG, "onBTv2 Connected called", mContext);
        try {
            // Storing the mac_id in a shared preference
            String mBlueToothMcId = bluetoothDevice.getAddress();
            SharedPreferences sharedPref = ((MyActivity) mContext).getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(MSwipeGateway.BLUETOOTH_MAC_ID, mBlueToothMcId).commit();
        } catch (Exception ex) {
            // TODO: Need to check why try-catch is used
            logger.error(TAG, ex.toString(), mContext);
        }
        // According to the demo app, need to sleep for some time
        DeviceConnectedWaitTask deviceConnectedWaitTask = new DeviceConnectedWaitTask(); //every time create new object, as AsynTask will only be executed one time.
        deviceConnectedWaitTask.execute();
    }

    @Override
    public void onBTv2Disconnected() {
        logger.debug(TAG, "onBTv2 Disconnected called", mContext);
        MSwipeGateway.deviceState = DeviceState.NOT_CONNECTED;
    }

    @Override
    public void onError(Error error) {

    }

    @Override
    public void onAudioDeviceNotFound() {

    }

    @Override
    public void onBTv4Connected() {

    }

    @Override
    public void onBTv4DeviceListRefresh(List<BluetoothDevice> list) {

    }

    @Override
    public void onBTv4Disconnected() {

    }

    @Override
    public void onBTv4ScanStopped() {

    }

    @Override
    public void onBTv4ScanTimeout() {

    }

    @Override
    public void onDevicePlugged() {

    }

    @Override
    public void onDeviceUnplugged() {

    }

    @Override
    public void onPrinterOperationEnd() {

    }

    @Override
    public void onRequestPrinterData(int i, boolean b) {

    }

    @Override
    public void onRequestVerifyID(String s) {

    }

    @Override
    public void onReturnAmount(String s, String s1) {

    }

    @Override
    public void onReturnCancelCheckCardResult(boolean b) {

    }

    @Override
    public void onReturnDisableInputAmountResult(boolean b) {

    }

    @Override
    public void onReturnEmvCardDataResult(boolean b, String s) {

    }

    @Override
    public void onReturnEnableInputAmountResult(boolean b) {

    }

    @Override
    public void onReturnEncryptDataResult(String s, String s1) {

    }

    @Override
    public void onReturnInjectSessionKeyResult(boolean b) {

    }

    @Override
    public void onReturnPhoneNumber(PhoneEntryResult phoneEntryResult, String s) {

    }

    @Override
    public void onReturnPrinterResult(PrinterResult printerResult) {

    }

    @Override
    public void onReturnReadTerminalSettingResult(TerminalSettingStatus terminalSettingStatus, String s) {

    }

    @Override
    public void onReturnUpdateTerminalSettingResult(TerminalSettingStatus terminalSettingStatus) {

    }

    // Helper functions
    private void resetCardDetails() {
        mSno = "";
        mCreditCardNo = "";
        mCardHolderName = "";
        mExpiryDate = "";
        mPaddingInfo = "";
        mEncTrackData = "";
        mEPB = "";
        mEPBKsn = "";
        mAmexSecurityCode = "";
        mAppIdentifier = "";
        mTVR = "";
        mTSI = "";
        mApplicationName = "";
        mCertif = "";
        mEmvCardExpdate = "";
        mSwitchCardType = "";
        mStandId = "";
        mF055tag = "";
        mAuthCode = "";
        mRRNo = "";
        mDate = "";
        mMid = "";
        mTid = "";
        mBatchNo = "";
        mIssuerAuthCode = "";
        mTlvDataCoAndC3 = "";
    }

    private void processMagCardData(CheckCardResult checkCardResult, Hashtable<String, String> decodeData) {
        logger.debug(TAG, "Processing Mag card data", mContext);
        MSwipeGateway.transactionType = com.ftl.paymentgateway.TransactionType.MICR;

        if (MSwipeGateway.test_mode) {
            logger.debug(TAG, "Test mode is enabled. Directly going to success screen", mContext);
            ((MyActivity) mContext).showCardSuccessScreen(mCreditCardNo, mCardHolderName);
            return;
        }
        SharedPreferences sharedPref = ((MyActivity) mContext).getPreferences(Context.MODE_PRIVATE);
        String wisepad_reference = sharedPref.getString(MSwipeGateway.WISEPAD_REFERENCE, "");
        String wisepad_session_token = sharedPref.getString(MSwipeGateway.WISEPAD_SESSION_TOKEN, "");
        String wisepad_mobile_no = sharedPref.getString(MSwipeGateway.WISEPAD_MOBILE_NO, "");


        mCardHolderName = decodeData.get("cardholderName") == null ? "" : decodeData.get("cardholderName");
        mCreditCardNo = decodeData.get("maskedPAN") == null ? "" : decodeData.get("maskedPAN");
        if (is_charge_card(mCreditCardNo, mCardHolderName)) {
            logger.debug(TAG, "Charging for charge card", mContext);
            // Creating an async task for the charging from charge card.
            // Because network operations are not allowed on the main thread
            PerformChargeCardTransactionTask chargeCardTransaction = new PerformChargeCardTransactionTask();
            chargeCardTransaction.execute();

        } else { // This is a normal swipe card
            // XXX: mAmexSecurityCode is only being set from an UI(which is not shown in Video),
            // Or from the callback of an activity which is not even called
            // So, this string is being set to "".

            logger.debug(TAG, "Charging for normal card", mContext);
            // Making the call to get the transaction done
            MSwipeGateway.wisePadController.CreditSale_MCR(CreditSaleHandler,
                    wisepad_reference,
                    wisepad_session_token,
                    "",
                    mAmexSecurityCode,
                    MSwipeGateway.amount,
                    wisepad_mobile_no,
                    "",
                    "");
        }

    }

    private boolean is_charge_card(String creditCardNo, String cardHolderName) {
        logger.debug(TAG, "Credit card no - " + creditCardNo + ". Card holder name - " + cardHolderName, mContext);
        int cardNoStart = -1;
        int cardHolderNameStart = -1;
        String first4Digits;
        if (mCreditCardNo.length() >= 4 ) {
            first4Digits = mCreditCardNo.substring(0, 4);
            logger.debug(TAG, "first 4 digits from creditcardno - [" + first4Digits + "]", mContext);
            if (first4Digits.equals(CHARGE_CARD_START)) {
                return true;
            }
        }

        if (cardHolderName.length() >= 4) {
            first4Digits = cardHolderName.substring(0, 4);
            logger.debug(TAG, "first 4 digits from cardholder name - [" + first4Digits + "]", mContext);
            if (first4Digits.equals(CHARGE_CARD_START)) {
                return true;
            }
        }

        return false;
    }

    public static Dialog getAppSelectDialog(Context context, String title) {
        Dialog dialog = new Dialog(context, R.style.styleCustDlg);
        dialog.setContentView(R.layout.dialog_app_select);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return true;
            }
            return false;
        }
        });

        dialog.setCancelable(true);

        // set the title
        TextView txttitle = (TextView) dialog.findViewById(R.id.tvmessagedialogtitle);
        txttitle.setText(title);
        return dialog;

    }

    private void storeTlvData(HashMap<String, String> tlv) {
        mCardHolderName = tlv.get("CardHolderName");
        mAppIdentifier = tlv.get("AppIdentifier");
        mCertif = tlv.get("Certif");
        mApplicationName = tlv.get("ApplicationName");
        mTVR = tlv.get("TVR");
        mTSI = tlv.get("TSI");
        mCreditCardNo = tlv.get("CreditCardNo");
        mExpiryDate = tlv.get("ExpiryDate");
    }

    // TODO: Make this class static
    private Handler CreditSaleHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            Bundle bundle = msg.getData();
            String responseMsg = bundle.getString("response");
            String errMsg = "";

            try
            {
                String[][] strTags = new String[][]{
                        {"status", ""}, {"ErrMsg", ""},
                        {"StandId", ""}, {"RRNO", ""},
                        {"AuthCode", ""}, {"Date", ""},
                        {"F055tag", ""}, {"EmvCardExpdate", ""},
                        {"SwitchCardType", ""}, {"IssuerAuthCode", ""},
                        };

                Constants.parseXml(responseMsg, strTags);

                String status = strTags[0][1];
                mswipeStatus = status;
                if (status.equalsIgnoreCase("false")) {
                    errMsg = strTags[1][1];
                    //the below has to be called for any erroneous response form the mswipe server
                    if (MSwipeGateway.transactionType == com.ftl.paymentgateway.TransactionType.EMV)
                        MSwipeGateway.wisePadController.sendOnlineProcessResult(null);

                } else if (status.equalsIgnoreCase("true")){

                    logger.info(TAG, "Status true returned from the mswipe server", mContext);
                    String StandId =  strTags[2][1];
                    Constants.StandId =StandId;

                    String RRNO =  strTags[3][1];
                    Constants.RRNO =RRNO;

                    String AuthCode =  strTags[4][1];
                    Constants.AuthCode =AuthCode;

                    String Date =  strTags[5][1];
                    Constants.TrxDate =Date;

                    mAuthCode = AuthCode;
                    mRRNo = RRNO;
                    mDate = Date;

                    /* XXX: Agniva - This might come to use later.
                    MSwipe_Payment_Details objPaymentDetails;
                    objPaymentDetails = GlobalVariables.getPayment_Details();
                    objPaymentDetails.set_MTT_REF1(objSettings.getMID());
                    objPaymentDetails.set_MTT_REF2(objSettings.getTID());
                    objPaymentDetails.set_BATCH_NO(objSettings.getBatchNo());
                    objPaymentDetails.set_MTT_REF3(mAuthCode);
                    objPaymentDetails.set_MTT_REF4(mRRNo);
                    GlobalVariables.setPayment_Details(objPaymentDetails);*/

                    if(MSwipeGateway.transactionType ==com.ftl.paymentgateway.TransactionType.EMV)
                    {
                        logger.info(TAG, "This is an EMV card. Trying with SendOnlineprocessResult.", mContext);
                        String F055tag =  strTags[6][1];
                        Constants.F055tag =F055tag;

                        String EmvCardExpdate =  strTags[7][1];
                        Constants.EmvCardExpdate =AuthCode;

                        String SwitchCardType =  strTags[8][1];
                        Constants.SwitchCardType =SwitchCardType;

                        String IssuerAuthCode =  strTags[9][1];
                        Constants.IssuerAuthCode =IssuerAuthCode;

                        mIssuerAuthCode = IssuerAuthCode;
                        mEmvCardExpdate = EmvCardExpdate;
                        mSwitchCardType = SwitchCardType;
                        mF055tag = F055tag;

                        // incase if the pin has been cancelled and the trx was submitted to the server then
                        //for the response back from the server send null to the emv device
                        if (isPinCanceled) {
                            MSwipeGateway.wisePadController.sendOnlineProcessResult(null);

                        } else {
                            //if the trx is successful and approved by the mswipe gateway send the response mIssuerAuthCode
                            //back to the emv device which check this issuecode for further validation
                            String tlvProcessResultData = mIssuerAuthCode;
                            MSwipeGateway.wisePadController.sendOnlineProcessResult(tlvProcessResultData);
                        }

                    }else{
                        logger.info(TAG, "This is a swipe card. Returning success to the client", mContext);
                        ((MyActivity) mContext).showCardSuccessScreen(mCreditCardNo, mCardHolderName);
                    }

                }else{
                    errMsg = "Invalid response from Mswipe server, please contact support.";
                }
            }
            catch (Exception ex) {
                errMsg = "Invalid response from Mswipe server, please contact support.";
            }

            if(errMsg.length() !=0)
            {
                ((MyActivity) mContext).showCardFailureScreen(errMsg);
                logger.error(TAG, errMsg, mContext);
            }
        }
    };

    private Handler voidSaleReversalHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {

            String errMsg;
            Bundle bundle = msg.getData();
            String responseMsg = bundle.getString("response");
            try
            {
                String[][] strTags = new String[][]{{"status", ""}, {"ErrMsg", ""},
                };
                Constants.parseXml(responseMsg, strTags);

                String status = strTags[0][1];
                if (status.equalsIgnoreCase("false")) {
                    errMsg = strTags[1][1];
                } else if (status.equalsIgnoreCase("true")){
                    errMsg = "Auto Reversal successful.";

                }else{
                    errMsg = "Invalid response from Mswipe server, please contact support.";
                }
            }
            catch (Exception ex) {
                errMsg = "Invalid response from Mswipe server, please contact support.";
            }

            if(errMsg.length() !=0)
            {
                ((MyActivity) mContext).showCardFailureScreen(errMsg);
                logger.error(TAG, errMsg, mContext);
            }
        }
    };


    private class AppSelectAdapter extends BaseAdapter {
        ArrayList<String> listData = null;
        Context context;

        public AppSelectAdapter(Context context, ArrayList<String> listData) {
            this.listData = listData;
            this.context = context;
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.appcustom_list_item, null);
            }

            TextView txtItem = (TextView) convertView.findViewById(R.id.menuview_lsttext);
            if (listData.get(position) != null)
                txtItem.setText(listData.get(position));
            return convertView;
        }
    }

    private class DeviceConnectedWaitTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                int ictr = 0;
                while (ictr < 3) {
                    Thread.sleep(500);
                    ictr++;
                }

            } catch (Exception ex) {
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            // Setting the device state to connected
            logger.debug(TAG, "Setting device state to connected", mContext);
            MSwipeGateway.deviceState = DeviceState.CONNECTED;
        }
    }

    private class PerformChargeCardTransactionTask extends AsyncTask<Void, Void, HttpResponse> {
        @Override
        protected HttpResponse doInBackground(Void... params) {
            SharedPreferences sharedPref = ((MyActivity) mContext).getPreferences(Context.MODE_PRIVATE);
            String inspirenetz_digest_auth = sharedPref.getString(MSwipeGateway.INSPIRENETZ_DIGEST_AUTH, "");
            String inspirenetz_username = sharedPref.getString(MSwipeGateway.INSPIRENETZ_USERNAME, "");
            String inspirenetz_password = sharedPref.getString(MSwipeGateway.INSPIRENETZ_PASSWORD, "");
            String inspirenetz_http_url = sharedPref.getString(MSwipeGateway.INSPIRENETZ_HTTP_URL, "");

            DefaultHttpClient httpclient = new DefaultHttpClient();

            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(inspirenetz_digest_auth,
                            80,
                            "user@inspirenetz.com"),
                    new UsernamePasswordCredentials(inspirenetz_username,
                            inspirenetz_password));

            List<String> authPrefs = new ArrayList<>(3);
            authPrefs.add(AuthPolicy.BASIC);
            authPrefs.add(AuthPolicy.NTLM);
            authPrefs.add(AuthPolicy.DIGEST);

            httpclient.getParams().setParameter("http.auth.scheme-priority",
                    authPrefs);

            String URL = String.format("%s/transaction/chargecard", inspirenetz_http_url);
            HttpPost httpPost = new HttpPost(URL);

            List<NameValuePair> nameValuePair = new ArrayList<>(2);

            nameValuePair.add(new BasicNameValuePair("purchase_amount", ""
                    + MSwipeGateway.amount));
            nameValuePair.add(new BasicNameValuePair("cardnumber",
                    mCardHolderName.trim()));

            // Url Encoding the POST parameters
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Making HTTP Request
            HttpResponse response = null;
            try {
                response = httpclient.execute(httpPost);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponse response) {
            try {
                HttpEntity entity = response.getEntity();
                String responseText = EntityUtils.toString(entity);
                logger.debug(TAG, "Response received from inspirenetz server is- " + responseText, mContext);
                // Set the JSONObject by parsing the response
                JSONObject retData = new JSONObject(responseText);

                retData.getString("status"); // getting status field
                retData.getString("errorcode");  // getting errorcode field


                if (retData.getString("status").equalsIgnoreCase("success")) {
                    logger.info(TAG, "Successful charge card transaction", mContext);
                    ((MyActivity) mContext).showCardSuccessScreen(mCreditCardNo, mCardHolderName);
                } else {
                    if (responseText.contains("ERR_NO_BALANCE")) {
                        logger.error(TAG, "Failure in ChargecardTransaction(): InSufficient Balance", mContext);
                        ((MyActivity) mContext).showCardFailureScreen("Insufficient balance");
                    } else if (responseText.contains("ERR_NOT_FOUND")) {
                        logger.error(TAG, "ChargecardTransaction(): No information found for the card number", mContext);
                        ((MyActivity) mContext).showCardFailureScreen("No information found for the card number");
                    } else if (responseText.contains("ERR_INVALID_CARD")) {
                        logger.error(TAG, "ChargecardTransaction(): The Charge Card/ Gift Card is invalid", mContext);
                        ((MyActivity) mContext).showCardFailureScreen("The Charge Card/ Gift Card is invalid");
                    } else if (responseText.contains("ERR_CARD_EXPIRED")) {
                        logger.error(TAG, "ChargecardTransaction(): The Charge Card / Gift Card has expired and can no longer be used", mContext);
                        ((MyActivity) mContext).showCardFailureScreen("The Charge Card / Gift Card has expired and can no longer be used");
                    } else if (responseText.contains("ERR_NO_AUTHENTICATION")) {
                        logger.error(TAG, "ChargecardTransaction(): Failure to authenticate with inspirenetz server", mContext);
                        ((MyActivity) mContext).showCardFailureScreen("Authentication failure with the server");
                    } else {
                        logger.error(TAG, "ChargecardTransaction(): Invalid Transaction", mContext);
                        ((MyActivity) mContext).showCardFailureScreen("Invalid Transaction");
                    }
                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}


