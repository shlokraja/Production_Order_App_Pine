package com.ftl.paymentgateway;

import android.content.Context;

/**
 * Created by rajamanickam.r on 6/28/2016.
 */
public interface IPaymentGateway {
    void Pay(String payment_amount,String sessionkey);
    boolean connectToDevice(boolean duringTransaction);
    void DisconnectDevice();
    void RevertTransaction();
    // This cancels the checkCard mode and brings the device to ready state
    void CancelCheckCard();
    void ShowCardSuccessScreen(String cardNo, final String cardholderName);
    void ShowCardFailureScreen(String displayMsg);
    void ShowBankSummary();
    String Getsessionkey(PaymentRequest data);
}
