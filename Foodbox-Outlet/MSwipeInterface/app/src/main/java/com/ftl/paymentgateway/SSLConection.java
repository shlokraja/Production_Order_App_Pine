package com.ftl.paymentgateway;

import android.util.Log;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLConection {
    private static TrustManager[] trustManagers;

    /* renamed from: functions.SSLConection.1 */
    static class C02281 implements HostnameVerifier {
        C02281() {
        }

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static class _FakeX509TrustManager implements X509TrustManager {
        private static final X509Certificate[] _AcceptedIssuers;

        static {
            _AcceptedIssuers = new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return _AcceptedIssuers;
        }
    }

    public static void allowAllSSL() {
        HttpsURLConnection.setDefaultHostnameVerifier(new C02281());
        if (trustManagers == null) {
            trustManagers = new TrustManager[]{new _FakeX509TrustManager()};
        }
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            Log.e("allowAllSSL", e.toString());
        } catch (KeyManagementException e2) {
            Log.e("allowAllSSL", e2.toString());
        }
    }
}
