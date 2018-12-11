package xyz.fz.http.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.fz.http.client.HttpClient;

import javax.annotation.Resource;

@Component
public class HttpClientRunner implements CommandLineRunner {

    @Resource
    private HttpClient httpClient;

    @Override
    public void run(String... args) throws Exception {
        httpClient.start();
    }
}
