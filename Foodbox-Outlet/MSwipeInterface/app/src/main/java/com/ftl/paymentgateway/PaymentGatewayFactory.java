package com.ftl.paymentgateway;

import com.ftl.mswipeinterface.MyActivity;

/**
 * Created by rajamanickam.r on 6/29/2016.
 */
public class PaymentGatewayFactory {
    public static IPaymentGateway paymentGateway(PaymentRequest paymentRequest,PaymentGateway paymentGateway, MyActivity myActivity) {
        switch (paymentGateway) {
            case MSwipeInterface:
                return new MSwipeGateway(myActivity);
            case Ongo:
                return new OngoGateway(paymentRequest,myActivity);
            case SingaporePayment:
                return new SingaporeGateway(paymentRequest,myActivity);
            case PineLabsPayment:
                return new PineLabsGateway(paymentRequest,myActivity);
            default:
                return null;
        }
    }
}

