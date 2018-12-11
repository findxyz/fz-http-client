package xyz.fz.http.client.ssl;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Properties;

public class SSLEngineFactory {

    private static SSLContext sslContext;

    static {
        initSslContext();
    }

    private static void initSslContext() {
        InputStream inputStream = null;
        try {
            Properties applicationProperties = PropertiesLoaderUtils.loadAllProperties("application.properties");
            String sslKeystore = applicationProperties.getProperty("ssl.keystore");
            inputStream = SSLEngineFactory.class.getResourceAsStream(sslKeystore);
            if (inputStream == null) {
                throw new RuntimeException("keystore file not found");
            }
            String sslKeystorePassword = applicationProperties.getProperty("ssl.keystore.password");

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(inputStream, sslKeystorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);
            TrustManager[] tms = trustManagerFactory.getTrustManagers();

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tms, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static SSLEngine create() {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(true);
        return sslEngine;
    }
}
