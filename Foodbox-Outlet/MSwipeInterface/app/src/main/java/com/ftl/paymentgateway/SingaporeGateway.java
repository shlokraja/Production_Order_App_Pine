package com.ftl.paymentgateway;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ftl.SingaporePaymentGateway.SingaporeGatewayTCPClient;
import com.ftl.SingaporePaymentGateway.SingaporePaymentGatewayResponse;
import com.ftl.mswipeinterface.MyActivity;

/**
 * Created by rajamanickam.r on 6/28/2016.
 */

public class SingaporeGateway implements IPaymentGateway {
    public Context myContext;
    private SingaporeGatewayTCPClient singaporeGatewayTCPClient;
    public String amount;
    public String Ip_Address;
    public String Port_Number;
    public  String responsemessage;
    private Thread t;
    public SingaporeGateway(PaymentRequest paymentRequest,MyActivity myActivity) {
        myContext = myActivity;
        Port_Number=paymentRequest.Port_Number;
        Ip_Address=paymentRequest.Ip_Address;
        amount=paymentRequest.Total_money;
        Log.i("SingaporeGateway ",Port_Number +"-----" +Ip_Address+"----------"+amount);
    }
    @Override
    public String Getsessionkey(PaymentRequest data){
        String sessionkey = "";
        return sessionkey;
    }

    @Override
    public void Pay(String payment_amount,String sessionkey) {
        Log.i("payment Amount ",Port_Number +"-----" +Ip_Address+"-------"+payment_amount);
        //  payment_amount="1";
        Log.i("payment Amount ",Port_Number +"-----" +Ip_Address+"-------"+payment_amount);
        SingaporeGatewayTCPClient myClient = new SingaporeGatewayTCPClient(Ip_Address.toString(),
                Integer.parseInt(Port_Number.toString()),
                payment_amount,"sale");
        myClient.execute();
        // ((MyActivity)myContext).myWebView.evaluateJavascript("$('#card_checkout .first_line').text('Be sure to pick up  1 Raitha');", null);

        t = new Thread() {

            @Override
            public void run() {
                try {
                    Log.i("Client","Started");
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        ((MyActivity)myContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // updateTextView();
                                responsemessage =SingaporePaymentGatewayResponse.responseMsg;
                                String messageResponseCode =SingaporePaymentGatewayResponse.responseCode;
                                Log.i("MessageString",responsemessage);
                                if(responsemessage.contains("INSERT OR TAP CARD"))
                                {
                                    responsemessage="INSERT OR TAP CARD";
                                }
                                else if(responsemessage.contains("PLEASE WAIT"))
                                {
                                    responsemessage="PLEASE WAIT, DO NOT REMOVE CARD";
                                }
                                else if(responsemessage.contains("REMOVE CARD"))
                                {
                                    responsemessage="REMOVE CARD";
                                }
                                else if(responsemessage.contains("XXXX"))
                                {
                                    responsemessage="PAYMENT SUCCESS";
                                }

                                if(messageResponseCode.equals("1")) {
                                    Log.i("code",messageResponseCode);
									((MyActivity)myContext).myWebView.evaluateJavascript("$('#card_checkout .swipe_card_btn_div .back_btn').addClass('hide');", null);
                                    ((MyActivity)myContext).myWebView.evaluateJavascript("$('#card_checkout .first_line').text('"+ responsemessage + "');", null);
                                    // Toast.makeText(((MyActivity)myContext), responsemessage, Toast.LENGTH_LONG).show();
                                }
                                else if(messageResponseCode.equals("2")) {
                                    ((MyActivity)myContext).myWebView.evaluateJavascript("$('#card_checkout .first_line').text('Be sure to pick up  1 Raitha');", null);
                                    Log.i("code",messageResponseCode);
                                    Log.i("Card No-- Name",SingaporePaymentGatewayResponse.cardNumber +"---"+SingaporePaymentGatewayResponse.cardholderName);
                                    messageResponseCode="";
                                    SingaporePaymentGatewayResponse.responseMsg="";
                                    SingaporePaymentGatewayResponse.responseCode="";
                                    ShowCardSuccessScreen(SingaporePaymentGatewayResponse.cardNumber,SingaporePaymentGatewayResponse.cardholderName);
                                    t.interrupt();
                                }
                                else if(messageResponseCode.equals("0"))
                                {
                                    Log.i("code",messageResponseCode);
                                    ShowCardFailureScreen(responsemessage);
                                    t.interrupt();

                                }
                            }
                        });
                    }
                } catch (Exception e) {
                }

            }
        };
        t.start();
    }

    @Override
    public boolean connectToDevice(boolean duringTransaction) {

        return false;
    }

    @Override
    public void DisconnectDevice() {

    }

    @Override
    public void RevertTransaction() {

    }

    @Override
    public void CancelCheckCard() {

    }

    @Override
    public void ShowCardSuccessScreen(String cardNo, final String cardholderName) {
        final String last4Digits;
        if (cardNo != "" && cardNo != null) {
            int ilen = cardNo.length();
            if (ilen >= 4)
                last4Digits = cardNo.substring(ilen - 4, ilen);
            else
                last4Digits = cardNo;
        } else {
            last4Digits = "";
        }
        ((MyActivity)myContext).myWebView.post(new Runnable() {
            @Override
            public void run() {
                ((MyActivity)myContext).myWebView.evaluateJavascript("showSuccessScreen('" + last4Digits + "','" + cardholderName + "')", null);
            }
        });
    }

    @Override
    public void ShowCardFailureScreen(String displayMsg) {
        final String msgToShow = displayMsg;
        ((MyActivity)myContext).myWebView.post(new Runnable() {
            @Override
            public void run() {
                ((MyActivity)myContext).myWebView.evaluateJavascript("showFailureScreen('" + msgToShow + "')", null);
            }
        });
    }

    @Override
    public void ShowBankSummary() {

    }
}
