package com.ftl.paymentgateway;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.http.SslCertificate;
import android.os.Build;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.ftl.mswipeinterface.MyActivity;
import com.ftl.mswipeinterface.MyLogger;

import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPermission;

/**
 * Created by rajamanickam.r on 6/28/2016.
 */
public class OngoGateway implements IPaymentGateway {
    private static final int REQUEST_ONGO_GATEWAY = 1;
    public static boolean isMopInstalled = false;
    private static Intent intent;
    public Context mContext;
    private MyLogger logger;
    private static final String NAMESPACE = "http://tempuri.org/";
    private static final String URL = "https://pos.indiatransact.com:7600/iso8583service.asmx";
    private static int timeout;

    static {
        timeout = 30000;
    }


    public String getDataFromUrl(PaymentRequest data) {
        String xml = null;
        try {
            logger.info(MyActivity.TAG,"New Session Key Start",mContext);
            SoapObject request = new SoapObject(NAMESPACE, "getSessionKey");
            request.addProperty("terminal_id", data.Terminal_id);
            request.addProperty("merchant_id", data.Merchant_id);
            request.addProperty("platform_type", "mPOS_integ_foodbox");
            request.addProperty("app_ver", "mpos_integ_version_v2.5");

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            SSLConection.allowAllSSL();
            try {
                new HttpTransportSE(URL).call(NAMESPACE + "getSessionKey", envelope);
            } catch (Exception e30) {
                e30.printStackTrace();
            }
            Object result = envelope.getResponse();
            if (result != null) {
                xml = result.toString();
            }
        } catch (Exception e302) {
            e302.printStackTrace();
        }
        logger.info(MyActivity.TAG,"New Session Key End" + xml,mContext);
        return  xml == null ? "No Response From Server" : xml;
    }

    public OngoGateway(PaymentRequest paymentRequest, MyActivity myActivity) {
        mContext = myActivity;
        logger = MyLogger.getInstance();
        try {

            //logger.info(MyActivity.TAG,sessionkeytest,mContext);
            logger.info(MyActivity.TAG,"OngoGateway Started",mContext);
            final PackageManager packageManager = ((MyActivity) mContext).getPackageManager();
            intent = new Intent("com.agsindia.mpos_integration.ActivitySwipe");
            List resolveInfo =
                    packageManager.queryIntentActivities(intent,
                            PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo.size() > 0) {
                isMopInstalled = true;
                intent.putExtra("serviceType", "sale");
                intent.putExtra("mer_id", paymentRequest.Merchant_id);
                intent.putExtra("ter_id", paymentRequest.Terminal_id);
                intent.putExtra("bt_address", paymentRequest.Bluetooth_id);
                intent.putExtra("bt_name", paymentRequest.Bluetooth_name);
                logger.info(MyActivity.TAG,"OngoGateway End",mContext);
            } else {
                isMopInstalled = false;
                logger.info(MyActivity.TAG,"Mpos Not Installed",mContext);
                Toast.makeText(((MyActivity) mContext), " mpos apk is Not installed.", Toast.LENGTH_LONG).show();
            }
        } catch (ActivityNotFoundException e) {
            logger.info(MyActivity.TAG,"Mpos Not Installed",mContext);
            Toast.makeText(((MyActivity) mContext), " mpos apk is Not installed.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void Pay(String payment_amount,String sessionkey) {
        logger.info(MyActivity.TAG,"Ongo Payment Start",mContext);
        if (isMopInstalled) {
            intent.putExtra("amount", payment_amount);
            intent.putExtra("sessionkey",sessionkey);
            logger.info(MyActivity.TAG,"Ongo Payment Activity Start",mContext);
            ((MyActivity) mContext).startActivityForResult(intent, REQUEST_ONGO_GATEWAY);
        } else {
            Toast.makeText(((MyActivity) mContext), " mpos apk is Not installed.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public String Getsessionkey(PaymentRequest data){
        String sessionkey = getDataFromUrl(data);
        return sessionkey;
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
        try {

            logger.info(MyActivity.TAG,"Ongo Payment Activity End",mContext);
            logger.info(MyActivity.TAG,"Ongo Success screen  Start",mContext);
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
            ((MyActivity) mContext).myWebView.post(new Runnable() {
                @TargetApi(Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    ((MyActivity) mContext).myWebView.evaluateJavascript("showSuccessScreen('" + last4Digits + "','" + cardholderName + "')", null);
                }
            });
            logger.info(MyActivity.TAG,"Ongo Success screen  End",mContext);
        }
        catch (Exception e) {
            logger.info(MyActivity.TAG,"Ongo Success screen  Error",mContext);
            Toast.makeText(((MyActivity) mContext), "Show Card Success Error",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void ShowCardFailureScreen(String displayMsg) {
        try {
            logger.info(MyActivity.TAG,"Ongo Payment Activity End",mContext);
            logger.info(MyActivity.TAG,"Ongo Failure screen  Start",mContext);
            final String msgToShow = displayMsg;
            ((MyActivity) mContext).myWebView.post(new Runnable() {
                @TargetApi(Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    ((MyActivity) mContext).myWebView.evaluateJavascript("showFailureScreen('" + msgToShow + "')", null);
                }
            });
            logger.info(MyActivity.TAG,"Ongo Failure screen  End",mContext);
        }
        catch (Exception e) {
            logger.info(MyActivity.TAG,"Ongo Failure screen  Error",mContext);
            Toast.makeText(((MyActivity) mContext), "Show Card Failure Error",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void ShowBankSummary() {

    }
}
