package com.ftl.SingaporePaymentGateway;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvaneshwari.r on 7/22/2016.
 */
public class SingaporePaymentGatewayResponseCode {
    Map<String, String> dictionary = new HashMap<String, String>();

    public void GetResponseValue() {
        dictionary.put("IC", "Invalid Card");
        dictionary.put("CE", "Connection Error / No Response From Host");
        dictionary.put("CO", "Error Reading Card");
        dictionary.put("XX", "Terminal Not Setup Properly");
        dictionary.put("EC", "Card is Expired");
        dictionary.put("RE", "Record Not Found");
        dictionary.put("TA", "Transaction Aborted");
        dictionary.put("BT", "Bad TLV Command Format");
        dictionary.put("CD", "Card Declined");
        dictionary.put("IN", "Invalid Card (EZLink)");
        dictionary.put("CT", "Card Read Timeout");
        dictionary.put("RT", "Response Timeout");
        dictionary.put("PE", "PIN ENTRY ERROR");

        dictionary.put("01", "Referral");
        dictionary.put("02", "Referral");
        dictionary.put("03", "Call Help SN");
        dictionary.put("05", "Do Not Honor");
        dictionary.put("12", "Call Help TR");
        dictionary.put("13", "Call Help AM");
        dictionary.put("14", "Call Help RE");
        dictionary.put("30", "Format Error");
        dictionary.put("31", "Call Help NS");
        dictionary.put("41", "Pick Up Card");
        dictionary.put("43", "Pick Up Card");
        dictionary.put("51", "Decline");
        dictionary.put("54", "Expired Card");
        dictionary.put("55", "Incorrect PIN");
        dictionary.put("91", "Switch Inoperative");
        dictionary.put("96", "System Malfunction");
        dictionary.put("**", "Undefined Error");
    }
}
