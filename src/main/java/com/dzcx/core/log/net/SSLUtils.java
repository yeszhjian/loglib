package com.dzcx.core.log.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * SSl for okhttp or urlconnection
 * Created by wangwq.2017/4/13
 */

public class SSLUtils {

    /**
     * 包装的SSL（Secure Socket Layer）参数类
     * 基于 okhttp
     */
    public static class SSLParams {
        public SSLSocketFactory sslSocketFactory;
        public X509TrustManager trustManager;
    }

    /**
     * @param certificates "xxx.cer"文件流（文件位置res/raw/xxx.cer）
     * @param bksFile      "xxx.bks"文件流（文件位置res/raw/xxx.bks）
     * @param password     the certificates's password
     * @return sslParams
     */
    public static SSLParams getSslSocketFactory(InputStream[] certificates, InputStream bksFile, String password) {
        SSLParams sslParams = new SSLParams();
        try {
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);

            //创建TLS类型的SSLContext对象，that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");

            X509TrustManager x509TrustManager;
            if (trustManagers != null) {
                x509TrustManager = new MyTrustManager(chooseTrustManager(trustManagers));
            } else {
                x509TrustManager = new UnSafeTrustManager();
            }

            //用上面得到的keyManagers初始化sslContext,这样sslContext就会信任keystore中的证书
            sslContext.init(keyManagers, new TrustManager[]{x509TrustManager}, null);
            sslParams.sslSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = x509TrustManager;
            return sslParams;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return sslParams;
    }

    private static TrustManager[] prepareTrustManager(InputStream... certificates) {
        if (certificates == null || certificates.length <= 0)
            return null;
        try {
            //创建X.509格式的certifiFactory
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            //创建默认类型的keystore,存储我们信任的证书
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                //java.security.cert 证书工厂根据证书文件流生成newCertificate
                Certificate newCertificate = certificateFactory.generateCertificate(certificate);
                //将证书newCertificate作为信任证书存放入keystore
                keyStore.setCertificateEntry(certificateAlias, newCertificate);
                try {
                    if (certificate != null) {
                        certificate.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //TrustManagerFactory是用于生成默认的TrustManager,这里创建一个默认类型的TrustManager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
        if (bksFile == null || password == null)
            return null;
        try {
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bksFile, password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class MyTrustManager implements X509TrustManager {
        private X509TrustManager defaultTrustManager;
        private X509TrustManager localTrustManager;

        public MyTrustManager(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            defaultTrustManager = chooseTrustManager(trustManagerFactory.getTrustManagers());
            this.localTrustManager = localTrustManager;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                localTrustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager)
                return (X509TrustManager) trustManager;
        }
        return null;
    }

    /**
     * 客户端不对证书做审核校验
     */
    private static class UnSafeTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * 基于urlConnection,信任所有
     */
    private static void TrustAllURL() {
        TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        try {
            SSLContext sslContext = SSLContext.getInstance("TSL");
            sslContext.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
