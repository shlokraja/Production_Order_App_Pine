package com.ftl.paymentgateway;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftl.PineLabsPaymentGateway.PineLabsPaymentGatewayResponse;
import com.ftl.PineLabsPaymentGateway.VolleySingleton;
import com.ftl.mswipeinterface.MyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPermission;
import java.util.Calendar;
import java.util.Date;
import java.io.UnsupportedEncodingException;


/**
 * Created by bhuvaneshwari.r on 18-Jul-17.
 */

public class PineLabsGateway implements IPaymentGateway {
    public Context myContext;
    public String amount;
    public String Merchant_id;
    public String Security_Token;
    public String IMEI;
    public String User_ID;
    public String Transaction_Number;
    public String Store_Pos_Code;
    public int SequenceNumber = 101;
    public  int startCount=0;
    public String transactionReferenceID;
    public static final int YEAR_TO_START = 2015;
    public static final int START_CONTER = 0;
    private Thread t;

    public PineLabsGateway(PaymentRequest paymentRequest, MyActivity myActivity) {
        myContext = myActivity;
        Merchant_id = paymentRequest.Merchant_id;
        Security_Token = paymentRequest.Security_Token;
        Store_Pos_Code = paymentRequest.Store_Pos_Code;
        Transaction_Number = paymentRequest.Transaction_Number;
        User_ID = paymentRequest.User_ID;
        IMEI = paymentRequest.IMEI;
        amount = paymentRequest.Total_money;
        SSLConection.allowAllSSL();
        SequenceNumber = hashCode();
        Log.v("SequenceNumber1----" + SequenceNumber, "--test");

        Log.i("PineLabsGateway ", Merchant_id + "-----" + Security_Token + "----------" + amount);
        Log.i("PineLabsGateway ", Store_Pos_Code + "-----" + Transaction_Number + "----------");
        Log.i("PineLabsGateway ", User_ID + "-----" + IMEI + "----------");

    }

    @Override
    public String Getsessionkey(PaymentRequest data) {
        String sessionkey = "";
        return sessionkey;
    }

    @Override
    public void Pay(String payment_amount, String sessionkey) {
        Log.i("payment Amount", "Pay" + sessionkey);

        amount = payment_amount;
        SequenceNumber = new Double(Math.random() * 1000000).intValue();
        startCount=0;
        Log.v("startCount---" + startCount, "--");
        Log.v("SequenceNumber2---" + SequenceNumber, "--test");
        final RequestQueue requestQueue = VolleySingleton.getInstance(myContext).getRequestQueue();

        final String url = "https://www.plutuscloudservice.in:8201/API/CloudBasedIntegration/V1/UploadBilledTransaction";
        final String mRequestBody = ReturnjsonObject_Payment(Transaction_Number + SequenceNumber, SequenceNumber, Store_Pos_Code, amount, User_ID, Merchant_id, Security_Token, IMEI).toString();
        Log.v("URL", url);
        Log.i("TSL version--"+android.os.Build.VERSION.SDK_INT,"");
        final PineLabsPaymentGatewayResponse paymnetResponse = new PineLabsPaymentGatewayResponse();
        StringRequest pinelabsPaymentRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("PineLabsDetails", response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            paymnetResponse.ResponseCode = responseObject.getString("ResponseCode").toString();
                            paymnetResponse.ResponseMessage = responseObject.getString("ResponseMessage").toString();
                            paymnetResponse.PlutusTransactionReferenceID = responseObject.getString("PlutusTransactionReferenceID").toString();
                            transactionReferenceID=paymnetResponse.PlutusTransactionReferenceID;
                            String insertMSg = "INSERT OR TAP CARD" + paymnetResponse.PlutusTransactionReferenceID;
                            String processingMSg = "Your Payment Processing with Transaction ID- " + paymnetResponse.PlutusTransactionReferenceID;
                            Log.v("PineLabsDetails msg--", insertMSg + "----" + processingMSg);
                            if (paymnetResponse.ResponseMessage.toString().matches("APPROVED")) {
                                ((MyActivity) myContext).myWebView.evaluateJavascript("$('#card_checkout .swipe_card_btn_div .back_btn').text('Cancel');", null);
                                ((MyActivity) myContext).myWebView.evaluateJavascript("$('#card_checkout .title').addClass('hide');", null);
                                ((MyActivity) myContext).myWebView.evaluateJavascript("$('#card_checkout .first_line').text('" + insertMSg + "');", null);
                                Thread.sleep(10000);

                                ((MyActivity) myContext).myWebView.evaluateJavascript("$('#card_checkout .first_line').text('" + processingMSg + "');", null);
                                t = new Thread() {

                                    @Override
                                    public void run() {
                                        try {
                                            Log.i("Check_Payment_Status", "Started");
                                            while (!isInterrupted()) {
                                                Thread.sleep(10000);
                                                ((MyActivity) myContext).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        startCount++;
                                                        Log.v("startCount---" + startCount, "--");
                                                        if(startCount<=15) {
                                                            Check_Payment_Status(paymnetResponse.PlutusTransactionReferenceID);
                                                        }
                                                        else{
                                                            Cancel_Payment(paymnetResponse.PlutusTransactionReferenceID,amount);
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
                        } catch (JSONException e) {
                            Log.v(" Pay responseObject", e.getMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.getMessage().contains("Unable to resolve host")){
                    ShowCardFailureScreen("Payment Cancelled,Check your internet connection!!!");
                }
                Log.v("Error response pay", error.getMessage());

            }
        }) {


            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }

        };

        Log.v("Before retry policy","hello");
        RetryPolicy retryPolicy = new DefaultRetryPolicy(100000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
       pinelabsPaymentRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(pinelabsPaymentRequest);

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
        Log.i("Cancel Check Card:","cancel");
        Log.i("Tansaction Reference id:",transactionReferenceID);
        Log.i("Tansaction Amount:", amount);
        Cancel_Payment(transactionReferenceID,amount);
        t.interrupt();
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
        ((MyActivity) myContext).myWebView.post(new Runnable() {
            @Override
            public void run() {
                ((MyActivity) myContext).myWebView.evaluateJavascript("showSuccessScreen('" + last4Digits + "','" + cardholderName + "')", null);
            }
        });
    }

    @Override
    public void ShowCardFailureScreen(String displayMsg) {
        final String msgToShow = displayMsg;
        ((MyActivity) myContext).myWebView.post(new Runnable() {
            @Override
            public void run() {
                ((MyActivity) myContext).myWebView.evaluateJavascript("showFailureScreen('" + msgToShow + "')", null);
            }
        });
    }

    @Override
    public void ShowBankSummary() {

    }

    public JSONObject ReturnjsonObject_Payment(String TransactionNumber, int SequenceNumber,
                                               String MerchantStorePosCode, String Amount, String UserID,
                                               String MerchantID, String SecurityToken, String IMEI) {

        Log.i("Pinelabs_Payment", TransactionNumber + "-----" + SequenceNumber + "----------" + UserID);
        Log.i("Pinelabs_Payment", MerchantStorePosCode + "-----" + Amount + "----------");
        Log.i("Pinelabs_Payment", MerchantID + "-----" + SecurityToken + "----------" + IMEI);

        JSONObject jResult = new JSONObject();
        try {
            int total_Amount =  (int) Float.parseFloat(Amount) * 100; // Convert paise into Rupees
            jResult.put("TransactionNumber", TransactionNumber);
            jResult.put("SequenceNumber", SequenceNumber);
            jResult.put("AllowedPaymentMode", "1");
            jResult.put("MerchantStorePosCode", MerchantStorePosCode);
            jResult.put("Amount", total_Amount);
            jResult.put("UserID", UserID);
            jResult.put("MerchantID", Integer.parseInt(MerchantID));
            jResult.put("SecurityToken", SecurityToken);
            jResult.put("IMEI", IMEI);
        } catch (Exception e) {
            t.interrupt();
            e.printStackTrace();
        }
        //Thread.sleep(4000);

        return jResult;
    }

    public JSONObject Payment_Status(String MerchantStorePosCode, String MerchantID,
                                     String SecurityToken, String IMEI, String PlutusTransactionReferenceID) {

        Log.i("Check_Payment", MerchantStorePosCode + "-----" + PlutusTransactionReferenceID + "----------");
        Log.i("Check_Payment", MerchantID + "-----" + SecurityToken + "----------" + IMEI);
        JSONObject jResult = new JSONObject();
        try {
            jResult.put("MerchantStorePosCode", MerchantStorePosCode);
            jResult.put("PlutusTransactionReferenceID", PlutusTransactionReferenceID);
            jResult.put("MerchantID", Integer.parseInt(MerchantID));
            jResult.put("SecurityToken", SecurityToken);
            jResult.put("IMEI", IMEI);
        } catch (Exception e) {
            t.interrupt();
            e.printStackTrace();
        }

        return jResult;
    }

    public void Check_Payment_Status(final String PlutusTransactionReferenceID) {
        Log.i("payment Amount", "Check_Payment_Status");
        RequestQueue requestQueue = VolleySingleton.getInstance(myContext).getRequestQueue();
        String url = "https://www.plutuscloudservice.in:8201/API/CloudBasedIntegration/V1/GetCloudBasedTxnStatus";
        final String mRequestBody = Payment_Status(Store_Pos_Code, Merchant_id, Security_Token, IMEI, PlutusTransactionReferenceID).toString();
        Log.v("URL", url);
        StringRequest pinelabsPaymentRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.v("Check_Payment_Status", response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            PineLabsPaymentGatewayResponse paymnetResponse = new PineLabsPaymentGatewayResponse();
                            paymnetResponse.ResponseCode = responseObject.getString("ResponseCode").toString();
                            paymnetResponse.ResponseMessage = responseObject.getString("ResponseMessage").toString();
                            paymnetResponse.PlutusTransactionReferenceID = responseObject.getString("PlutusTransactionReferenceID").toString();
                            if (paymnetResponse.ResponseMessage.toString().matches("TXN APPROVED")) {
                                JSONArray transactionJSONArray = responseObject.getJSONArray("TransactionData");
                                String cardNumber = "";
                                String cardholderName = "";
                                for (int transaction = 0; transaction <= transactionJSONArray.length(); transaction++) {
                                    if (responseObject.getJSONArray("TransactionData").getJSONObject(transaction).get("Tag").toString().matches("Card Number")) {
                                        cardNumber = responseObject.getJSONArray("TransactionData").getJSONObject(transaction).get("Value").toString();
                                    } else if (responseObject.getJSONArray("TransactionData").getJSONObject(transaction).get("Tag").toString().matches("Card Type")) {
                                        cardholderName = responseObject.getJSONArray("TransactionData").getJSONObject(transaction).get("Value").toString();
                                    }
                                    Log.v("PineLabsDetails", cardNumber + "-----" + cardholderName);
                                    if (cardNumber != "") {
                                        break;
                                    }
                                }
                                if (cardNumber != "") {
                                    ((MyActivity) myContext).myWebView.evaluateJavascript("$('#card_checkout .title').addClass('show');", null);
                                    ((MyActivity) myContext).myWebView.evaluateJavascript("$('#card_checkout .swipe_card_btn_div .back_btn').addClass('show');", null);
                                    ShowCardSuccessScreen(cardNumber, "PineLabs");
                                    t.interrupt();
                                }
                            } else if( !(paymnetResponse.ResponseMessage.toString().matches("TXN APPROVED")|| paymnetResponse.ResponseMessage.toString().matches("TXN UPLOADED")) ) {
                            // code added to cancel the failed transaction jagadesh 11-11-2017
                                // ((MyActivity)myContext).myWebView.evaluateJavascript("$('#card_checkout .first_line').text("+paymnetResponse.ResponseMessage+");", null);
                                //Thread.sleep(20000);
                                // Check_Payment_Status(paymnetResponse.PlutusTransactionReferenceID);
                                //ShowCardSuccessScreen("123456","Test");
                                Cancel_Payment(PlutusTransactionReferenceID,amount);
                                t.interrupt();
                            }
                        } catch (JSONException e) {
                            t.interrupt();
                            Log.v("status responseObject", e.getMessage());
                        } catch (Exception e) {
                            t.interrupt();
                            Log.v("stserror responseObject", e.getMessage());
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("check responseObject", error.getMessage());

            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }

        };

        RetryPolicy retryPolicy = new DefaultRetryPolicy(80000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        pinelabsPaymentRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(pinelabsPaymentRequest);

    }

    public JSONObject Cancel_Payment_Status(String MerchantStorePosCode, String MerchantID,
                                            String SecurityToken, String IMEI, String PlutusTransactionReferenceID, String Amount) {
        int total_Amount =(int) Float.parseFloat(Amount) * 100; // Convert paise into Rupees
        Log.i("Cancel_Payment_Status", MerchantStorePosCode + "-----" + PlutusTransactionReferenceID + "----------");
        Log.i("Cancel_Payment_Status", MerchantID + "-----" + SecurityToken + "----------" + IMEI+"--Amount--"+total_Amount);
        JSONObject jResult = new JSONObject();
        try {
            jResult.put("MerchantStorePosCode", MerchantStorePosCode);
            jResult.put("PlutusTransactionReferenceID", PlutusTransactionReferenceID);
            jResult.put("MerchantID", Integer.parseInt(MerchantID));
            jResult.put("SecurityToken", SecurityToken);
            jResult.put("IMEI", IMEI);
            jResult.put("Amount", total_Amount);
        } catch (Exception e) {
            t.interrupt();
            e.printStackTrace();
        }

        return jResult;
    }

    public void Cancel_Payment(String PlutusTransactionReferenceID, String Amount) {
        Log.i("payment Amount", "Cancel_Payment");
        Log.i("payment Amount", "-Store_Pos_Code-"+Store_Pos_Code+"-Merchant_id-"+Merchant_id+"-Security_Token-"+Security_Token+"-IMEI-"+
                IMEI+"-PlutusTransactionReferenceID-"+PlutusTransactionReferenceID+"-Amount-"+Amount);
        RequestQueue requestQueue = VolleySingleton.getInstance(myContext).getRequestQueue();
        String url = "https://www.plutuscloudservice.in:8201/API/CloudBasedIntegration/V1/CancelTransaction";
        final String mRequestBody = Cancel_Payment_Status(Store_Pos_Code, Merchant_id, Security_Token, IMEI, PlutusTransactionReferenceID, Amount).toString();
        Log.v("Cancel_Payment URL", url);
        StringRequest pinelabsPaymentRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.v("Cancel_Payment", response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            PineLabsPaymentGatewayResponse paymnetResponse = new PineLabsPaymentGatewayResponse();
                            paymnetResponse.ResponseCode = responseObject.getString("ResponseCode").toString();
                            paymnetResponse.ResponseMessage = responseObject.getString("ResponseMessage").toString();

                            if (paymnetResponse.ResponseMessage.toString().matches("APPROVED")) {
                                ((MyActivity) myContext).myWebView.evaluateJavascript("$('#card_checkout .title').addClass('show');", null);
                                ((MyActivity)myContext).myWebView.evaluateJavascript("$('#card_checkout .swipe_card_btn_div .back_btn').addClass('show');", null);
                                ShowCardFailureScreen("Payment Cancelled,Please try again!!!");
                                t.interrupt();
                                //((MyActivity)myContext).myWebView.evaluateJavascript("$('#card_checkout .first_line').text('Payment Cancelled,Please try again!!!');", null);
                            }
                            else{
                                ((MyActivity) myContext).myWebView.evaluateJavascript("$('#card_checkout .title').addClass('show');", null);
                                ((MyActivity)myContext).myWebView.evaluateJavascript("$('#card_checkout .swipe_card_btn_div .back_btn').addClass('show');", null);
                                ShowCardFailureScreen("Payment Cancelled,Please try again!!!");
                                t.interrupt();
                            }

                        } catch (JSONException e) {
                            t.interrupt();
                            Log.v("cancel responseObject", e.getMessage());
                        } catch (Exception e) {
                            t.interrupt();
                            Log.v("cl error responseObject", e.getMessage());
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("error-cel response", error.getMessage());

            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }

        };

        RetryPolicy retryPolicy = new DefaultRetryPolicy(80000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        pinelabsPaymentRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(pinelabsPaymentRequest);

    }


}
