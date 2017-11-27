package com.ftl.SingaporePaymentGateway;

/**
 * Created by bhuvaneshwari.r on 7/22/2016.
 */
public class SingaporePaymentGatewayResponse {
    public static String responseCode;
    public static String responseMsg;
    public static String cardholderName;
    public static String cardNumber;
    public static void setResponse(String code, String msg,String cardNo,String name)
    {
        responseCode=code;
        responseMsg=msg;
        cardholderName=name;
        cardNumber=cardNo;
    }
}
