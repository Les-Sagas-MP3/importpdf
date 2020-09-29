package fr.lessagasmp3.importpdf.service;

import fr.lessagasmp3.core.constant.Strings;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

@Service
public class HttpClientService {

    private static final String HTTP_PROXY = System.getenv("HTTP_PROXY");

    public HttpClient getHttpClient() {

        HttpClient httpClient;

        if(HTTP_PROXY != null) {
            String[] proxy = HTTP_PROXY.replace("http://", Strings.EMPTY).replace("https://", Strings.EMPTY).split(":");
            httpClient = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(new InetSocketAddress(proxy[0], Integer.parseInt(proxy[1]))))
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        } else {
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        }

        return httpClient;
    }

}
