package xyz.fz.http.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.SecureRandom;

@Configuration
public class BeanConfiguration {

    @Value("${ssl.keystore}")
    private String sslKeystore;

    @Value("${ssl.keystore.password}")
    private String sslKeystorePassword;

    @Bean
    public SSLEngine httpSSLEngine() throws Exception {
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(this.getClass().getResourceAsStream(sslKeystore), sslKeystorePassword.toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keystore);
        TrustManager[] tms = trustManagerFactory.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tms, new SecureRandom());
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(true);
        return sslEngine;
    }
}
