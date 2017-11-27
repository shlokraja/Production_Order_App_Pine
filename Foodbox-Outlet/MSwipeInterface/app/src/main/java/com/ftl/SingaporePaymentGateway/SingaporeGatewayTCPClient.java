package com.ftl.SingaporePaymentGateway;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ftl.mswipeinterface.MyLogger;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;


/**
 * Created by bhuvaneshwari.r on 7/22/2016.
 */
public class SingaporeGatewayTCPClient extends AsyncTask<Void, Void, Void > {

    SingaporePaymentGatewayResponseCode paymentResponseCode=new SingaporePaymentGatewayResponseCode();

    String terminalIPAddress;
    int terminalPortNumber;
    String  response = "";
    public String saleCommand;
    public int inRunningNumber = 1;
    String responseString = "";
    private MyLogger logger;
    Socket socket = null;
    public static final String TAG = SingaporeGatewayTCPClient.class.getSimpleName();
    private static Context context;

    public SingaporeGatewayTCPClient(String addr, int port, String amount, String command) {
        terminalIPAddress = addr;
        terminalPortNumber = port;
        logger = MyLogger.getInstance();
        if(command=="sale")
        {
            this.createSaleCommand(amount);
        }
       Log.i(TAG,"Singapore-TCPClient "+terminalIPAddress +"-----" +terminalPortNumber+"-------"+saleCommand);

    }
    @Override
    protected Void doInBackground(Void... args0) {
        try {
           Log.i(TAG,"Socket "+ terminalIPAddress +"-----" +terminalPortNumber+"-------"+saleCommand);
            boolean isconnected=true;
            isconnected=isConnectedToServer();
           Log.i(TAG,"Connection "+"---"+isconnected);
            if (isconnected) {
                //socket = new Socket(dstAddress, dstPort);
               Log.i(TAG,"Socket connected "+ "socket not null");

               Log.i(TAG,"Socket "+ "socket not null");
                final OutputStream outToServer = socket.getOutputStream();
                DataOutputStream outputStream = new DataOutputStream(outToServer);
                byte[] saleCommandBytes = saleCommand.getBytes();

                outputStream.write(saleCommandBytes);
               Log.i(TAG,"Socket Output"+ "Success" );
                //inputstream reader
                final InputStream inputStream = socket.getInputStream();
                int bytesToRead = 0;
               Log.i(TAG,"Socket Input"+ "before");
                byte[] buffer = new byte[1024];


                int responseLine = 0;
                // read bytes from stream, and store them in buffer
                while ((responseLine = inputStream.read(buffer)) != -1) {

                    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                    // Writes bytes from byte array (buffer) into output stream.
                    arrayOutputStream.write(buffer, 0, responseLine);
                   Log.i(TAG,"Response Array: "+ arrayOutputStream.toString());
                    responseString = arrayOutputStream.toString();
                   Log.i(TAG,"Response String: "+ responseString);
                    SingaporePaymentGatewayResponse.setResponse("1", responseString, "", "");

                }
                String[] responseResult = getTagValue(responseString, 39);
                if (responseResult != null) {
                    String responseMsg = "";
                    paymentResponseCode.GetResponseValue();
                    //Log.i(TAG,("Response Msg1: ", responseResult[1] + " " + responseResult[0]);

                    if (responseResult[0].equals("0")) {
                       Log.i(TAG,"Response Msg2: "+ responseResult[1]);

                        if (responseResult[1].equals("00")) {
                            SingaporePaymentGatewayResponse.setResponse("1", "Approved", "", "");
                           Log.i(TAG,"Response Msg3: "+ responseResult[1]);
                            responseMsg = "Approved";
                            SingaporePaymentGatewayResponse.setResponse("1", responseMsg, "", "");
                            String stReceiptValue = "";
                            StringBuilder stReceiptValue1 = new StringBuilder();
                            String stValue;
                            String stAmount;
                            String[] responseDate = getTagValue(responseString, 7);
                            String[] responseAmount = getTagValue(responseString, 4);
                            String[] responseCardType = getTagValue(responseString, 54);
                            String[] responseCardNumber = getTagValue(responseString, 2);
                            String[] responseInvoice = getTagValue(responseString, 62);
                            String[] responseCardHolderName = getTagValue(responseString, 98);
                           Log.i(TAG,"Payment Details"+ responseDate[1] + "-----" + responseAmount[1]);
                            if (responseDate[0].equals("0")) {
                                stReceiptValue += "DATE TIME: ";
                                stReceiptValue += responseDate[1].substring(2, 4); // DD
                                stReceiptValue += "/";
                                stReceiptValue += responseDate[1].substring(0, 2); // MM
                                stReceiptValue += " ";
                                stReceiptValue += responseDate[1].substring(4, 6);
                                stReceiptValue += ":";
                                stReceiptValue += responseDate[1].substring(6, 8);
                                stReceiptValue += ":";
                                stReceiptValue += responseDate[1].substring(8, 10);
                                stReceiptValue += "\r\n";
                            }
                            if (responseAmount[0].equals("0")) {
                                stReceiptValue += "AMOUNT:   ";
                                stReceiptValue += "$";
                                stReceiptValue += responseAmount[1].substring(0, 10);
                                stReceiptValue += ".";
                                stReceiptValue += responseAmount[1].substring(10, 12);
                               Log.i(TAG,"Payment Approved Amount"+ stReceiptValue);
                            }
                            if (responseCardType[0].equals("0")) {
                                //Card Type
                                stReceiptValue += responseCardType[1];
                                stReceiptValue += " ";
                               Log.i(TAG,"Payment Approved Card"+ stReceiptValue);
                            }
                            if (responseCardNumber[0].equals("0")) {
                                //// card number
                                stReceiptValue += responseCardNumber[1];
                                stReceiptValue += "\r\n";
                               Log.i(TAG,"Payment Approved CardNo"+stReceiptValue);
                            }
                            if (responseCardHolderName[0].equals("0")) {
                                //Card Holder Name
                                responseCardHolderName[1] = responseCardHolderName[1].replace("/", "");
                                stReceiptValue += responseCardHolderName[1].trim();
                                stReceiptValue += " ";
                               Log.i(TAG,"Card Holder Name---"+ responseCardHolderName[1]);
                               Log.i(TAG,"Payment Approved Name"+ stReceiptValue);
                            }
                            if (responseInvoice[0].equals("0")) {
                                stReceiptValue += "INVOICE: ";
                                stReceiptValue += responseInvoice[1];
                                stReceiptValue += "  ";
                               Log.i(TAG,"Payment -Invoice"+ stReceiptValue);
                            }

                            SingaporePaymentGatewayResponse.setResponse("2", responseString, responseCardNumber[1], responseCardHolderName[1].trim());

                        } else {
                           Log.i(TAG,"Response Msg: "+ paymentResponseCode.dictionary.get(responseResult[1].toString()));
                            responseMsg = paymentResponseCode.dictionary.get(responseResult[1].toString());
                            SingaporePaymentGatewayResponse.setResponse("0", responseMsg, "", "");
                        }
                    } else {
                       Log.i(TAG,"Response Msg: "+ paymentResponseCode.dictionary.get(responseResult[1].toString()));
                        responseMsg = paymentResponseCode.dictionary.get(responseResult[1].toString());
                        SingaporePaymentGatewayResponse.setResponse("0", responseMsg, "", "");

                    }

                } else {
                    SingaporePaymentGatewayResponse.setResponse("1", responseString, "", "");
                }
            }
            else {
               Log.i(TAG,"socket null "+ "socket null");
                SingaporePaymentGatewayResponse.setResponse("0", "Please check the internet connection", "", "");
            }


        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
           Log.i(TAG,"Error1 "+ e.getMessage());
            response = "UnknownHostException: " + e.toString();
            SingaporePaymentGatewayResponse.setResponse("0", response, "", "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
           Log.i(TAG,"Error2 "+ e.getMessage());
            response = "IOException: " + e.toString();
            SingaporePaymentGatewayResponse.setResponse("0", response, "", "");
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            //  e.printStackTrace();
           Log.i(TAG,"Error3 "+ e.getMessage());
            response = "Exception: " + e.toString();
            SingaporePaymentGatewayResponse.setResponse("0", response, "", "");
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    //  e.printStackTrace();
                }
            }
        }


        return null;


    }

    public boolean isConnectedToServer() {
        boolean isConnected = false;
        try {
           Log.i(TAG,"isConnectedToServer "+"---"+"Started");
            SocketAddress sockaddr = new InetSocketAddress(terminalIPAddress, terminalPortNumber);
           Log.i(TAG,"isConnectedToServer "+"---"+"sockaddr");
            // Create an unbound socket
            socket = new Socket();
           Log.i(TAG,"isConnectedToServer "+"---"+"new socket");
            // This method will block no more than timeoutMs.
            // If the timeout occurs, SocketTimeoutException is thrown.
            int timeoutMs = 5000;   // 5 seconds
           Log.i(TAG,"isConnectedToServer "+"---"+"set timer");
            socket.connect(sockaddr, timeoutMs);
           Log.i(TAG,"isConnectedToServer"+" connected");
            isConnected = true;
        } catch (Exception e) {
           Log.i(TAG,"isConnectedToServer"+" Not connected");
            return isConnected;
        }
        return isConnected;
    }

    public void createSaleCommand(String payment_amount) {
        saleCommand="C200";
        double amount=Double.valueOf(payment_amount)*100;
       Log.i(TAG,"SaleCommand - Amount--"+" "+amount);
        int paymentValue=(int) amount;
       Log.i(TAG,"Sale paymentValue--"+" "+ paymentValue);
        payment_amount= Integer.toString(paymentValue);
        //payment_amount=payment_amount.replace(".0","");
       Log.i(TAG,"SaleCommand - Amount--"+payment_amount);
        if (saleCommand.length() > 0 && saleCommand.length() <= 12)
        {
            saleCommand =saleCommand.concat("0412");
            saleCommand =saleCommand.concat(padLeftZeros(payment_amount, 12));
            saleCommand =saleCommand.concat("5706");
            saleCommand= saleCommand.concat(padLeftZeros(Integer.toString(inRunningNumber),6));
            this.incrementRunningNumber();
           Log.i(TAG,"createSaleCommand: "+ saleCommand);
        }
    }

    public static String padLeftZeros(String str, int n) {
        return String.format("%1$" + n + "s", str).replace(' ', '0');
    }
    public final String[] getTagValue(String ResponseString, int inTag )
    {
        int i = 0;
        String tag;
        String length;
        String value;
        String ar[] = new String[2];
        ar[0]= "";
        ar[1] =  "";

       Log.i(TAG,"ResponseString Device"+ResponseString);
        if (ResponseString == null)
        {
           Log.i(TAG,"ResponseString Device Null");
            return ar;
        }
        for (i = 4; i < ResponseString.length();)
        {
            tag = ResponseString.substring(i, i + 2);
            i += 2;
            length = ResponseString.substring(i, i + 2);
            i += 2;
            if (Integer.parseInt(length) > 0)
            {
                value = ResponseString.substring(i, i + Integer.parseInt(length));

                if (Integer.parseInt(tag) == inTag)
                {
                    ar[1] = value;
                    ar[0]="0";
                   Log.i(TAG,("ResponseString Device "+ar[1]+" "+ar[0]));
                    return ar;
                }
                i += Integer.parseInt(length);
            }

        }

        ar[1] = "";
        ar[0]="1";; // Error tag not found

       Log.i(TAG,("ResponseString Device "+ar[1]+" "+ar[0]));
        return ar;

    }


    private void incrementRunningNumber()
    {
        inRunningNumber++;
        if (inRunningNumber > 999999)
            inRunningNumber = 1;
    }
}
