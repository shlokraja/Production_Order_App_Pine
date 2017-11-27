package com.ftl.mswipeinterface;


import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;

public class Constants {

    public static String StandId = "";
    public static String RRNO = "";
    public static String AuthCode = "";
    public static String TrxDate = "";

    public static String F055tag = "";
    public static String EmvCardExpdate = "";
    public static String SwitchCardType = "";
    public static String IssuerAuthCode = "";

    public static void parseXml(String xmlString,String[][] strTags ) throws Exception {

        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(new StringReader(xmlString));
            int eventType = XmlPullParser.START_TAG;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                eventType = parser.next();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String xmlTag = parser.getName();
                        for (int iTagIndex = 0; iTagIndex < strTags.length; iTagIndex++) {
                            if (strTags[iTagIndex][0].equals(xmlTag)) {
                                eventType = parser.next();
                                if (eventType == XmlPullParser.TEXT) {
                                    String xmlText = parser.getText();


                                    strTags[iTagIndex][1] = ( xmlText == null ? "" : xmlText);// store the key
                                } else if (eventType == XmlPullParser.END_TAG) {

                                    strTags[iTagIndex][1]  =  "";
                                }
                                break;
                            }
                        }
                        break;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (parser != null) {
                parser = null;
            }
        }

    }
    // change password and Login
    /*public static final String PWD_DIALOG_MSG = "Change Password";
    public static final String PWD_ERROR_INVALIDPWD = "Invalid Password! Cannot be blank.";
    public static final String PWD_ERROR_INVALIDPWDLENGTH = "Minimum length of the password should be 6 characters.";
    public static final String PWD_ERROR_INVALIDPWDMAXLENGTH = "Length of the password should be between 6 and 10 characters.";

    public static final String PWD_ERROR_INVALIDPWDNEWLENGTH = "Minimum length of the new password should be 6 characters.";
    public static final String PWD_ERROR_INVALIDPWDMAXNEWLENGTH = "Length of the new password should be between 6 and 10 characters.";
    public static final String PWD_ERROR_INVALIDPWDRETYPELENGTH = "Minimum length of the re-entered password should be 6 characters.";
    public static final String PWD_ERROR_INVALIDNEWANDRETYPE = "New passwords do not match.";
    public static final String PWD_CHANGEPWD_CONFIRMATION = "Would you like to change the password?";
    public static final String PWD_ERROR_PROCESSIING_DATA = "Error in processing your change password request.";

    // card sale
    public static final String CARDSALE_DIALOG_MSG = "Atchayam Notification";
    public static final String CARDSALE_ERROR_INVALIDAMT = "Invalid amount! Minimum amount should be Rs. 0.01 to proceed.";
    public static final String CARDSALE_ERROR_INVALIDCARDDIGITS = "Invalid last 4 digits.";
    public static final String CARDSALE_ALERT_AMOUNTMSG = "The total card sale amount is %s ";

    public static final String CARDSALE_ALERT_swiperAMOUNTMSG = "Total amount of this transaction \nis %s ";
    public static final String CARDSALE_ERROR_mobilenolen = "Required length of the mobile number is %d digits.";
    public static final String CARDSALE_ERROR_mobileformat = "The mobile number cannot start with 0.";
    public static final String CARDSALE_ERROR_receiptvalidation = "Please enter a valid Receipt Number.";
    public static final String CARDSALE_ERROR_receiptmandatory = "Receipt mandatory for this transaction, please un check the field to proceed";


    public static final String CARDSALE_ERROR_email = "Invalid e-mail address.";
    public static final String CARDSALE_ERROR_LstFourDigitsNotMatched = "Last 4 digits don't match, bad card.";
    public static final String CARDSALE_ERROR_PROCESSIING_DATA = "Error in processing Card Sale.";

    public static final String CARDSALE_AMEX_Validation = "Invalid Amex card security code.";

    public static final String CARDSALE_Sign_Validation = "Receipt needs to be signed to proceed.";
    public static final String CARDSALE_Sign_SUCCESS_Msg = "Receipt successfully uploaded to " + ApplicationData.SERVER_NAME + " server.";

    public static final String CARDSALE_AUTO_REVERSAL = "Auto Reversal successfull.";
    public static final String CARDSALE_ERROR_FO35 = "Error in processing Card Sale.";


    public static final String CARDSALE_Device_Connect_Msg = "WisePad not connected, please make sure the WisePad is switched on.";
    public static final String CARDSALE_Device_Connecting_Msg = "Connecting to  Wisepad, if its taking longer then usual, please restart the WisePad and try re-connecting.";
    public static final String CASHSALE_SERVER_CONNECTIVITY_MSG = "Cash Machine Communicates with other counter. Trying to connect. Please wait...";

    // last transaction
    public static final String LSTTRXST_DIALOG_MSG = "Last Tx Status";
    public static final String LSTTRXST_ERROR_FETCHING_DATA = "Error in fetching last tx details.";
    public static final String LSTTRXST_ERROR_Processing_DATA = "Error in processing the last transaction request.";
    public static final String LSTTRXST_Success_msg = "The receipt was successfully resent.";
    // Login
    public static final String LOGIN_DIALOG_MSG = "Login";
    public static final String LOGIN_ERROR_ValidUserMsg = "Please enter a valid User Id and Password.";
    public static final String LOGIN_ERROR_Processing_DATA = "Unable to login, please try again.";

    // void
    public static final String VOID_DIALOG_MSG = "Void Sale";
    public static final String VOID_ERROR_Processing_DATA = "Error in processing the void sale request.";
    public static final String VOID_ERROR_Processing_FLAG = "Error in updating the void sale flag.";
    public static final String VOID_ALERT_AMOUNTMSG = "Proceed to void card sale of %s %s for the card ending with last 4 digits %s?";
    public static final String VOID_ALERT_FORVOID = "Would you like to VOID the selected transaction dated %s of %s %s for the card with the last 4 digits %s?";*/
}