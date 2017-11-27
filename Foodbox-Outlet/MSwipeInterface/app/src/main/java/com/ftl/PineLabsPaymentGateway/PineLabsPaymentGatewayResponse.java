package com.ftl.PineLabsPaymentGateway;

import java.util.List;

/**
 * Created by bhuvaneshwari.r on 31-Jul-17.
 */

public class PineLabsPaymentGatewayResponse {
    public String ResponseCode;
    public String ResponseMessage;
    public String PlutusTransactionReferenceID;
    public List<TransactionData>list;
}
class TransactionData{
    String Tag,Value;
}