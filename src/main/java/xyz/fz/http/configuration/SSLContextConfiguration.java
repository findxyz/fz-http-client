package xyz.fz.http.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@Configuration
public class SSLContextConfiguration {
    @Value("${ssl.keystore}")
    private String sslKeystore;

    @Value("${ssl.keystore.password}")
    private String sslKeystorePassword;

    @Value("${ssl.enable}")
    private boolean sslEnable;

    @Bean
    public SSLContext sslContext() {
        if (sslEnable) {
            return yesSSL();
        } else {
            return noSSL();
        }
    }

    private SSLContext yesSSL() {
        SSLContext sslContext = null;
        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getResourceAsStream(sslKeystore);
            if (inputStream == null) {
                throw new RuntimeException("keystore file not found");
            }

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
        return sslContext;
    }

    private SSLContext noSSL() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;
    }
}
