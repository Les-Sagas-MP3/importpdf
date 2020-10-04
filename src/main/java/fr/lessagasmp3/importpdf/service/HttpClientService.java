package fr.lessagasmp3.importpdf.service;

import fr.lessagasmp3.core.constant.Strings;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

@Service
public class HttpClientService {

    private static final String HTTP_PROXY = System.getenv("HTTP_PROXY");

    public CloseableHttpClient getHttpClient() {

        CloseableHttpClient httpClient;

        if(HTTP_PROXY != null) {
            String[] proxyStr = HTTP_PROXY.replace("http://", Strings.EMPTY).replace("https://", Strings.EMPTY).split(":");
            HttpHost proxy = new HttpHost(proxyStr[0], Integer.parseInt(proxyStr[1]));
            HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy) {
                @Override
                public HttpRoute determineRoute(
                        final HttpHost host,
                        final HttpRequest request,
                        final HttpContext context) throws HttpException {
                    String hostname = host.getHostName();
                    if (hostname.equals("127.0.0.1") || hostname.equalsIgnoreCase("localhost")) {
                        // Return direct route
                        return new HttpRoute(host);
                    }
                    return super.determineRoute(host, request, context);
                }
            };
            httpClient = HttpClients.custom()
                    .setRoutePlanner(routePlanner)
                    .build();
        } else {
            httpClient = HttpClients.custom()
                    .build();
        }

        return httpClient;
    }

    public String getStringResponse(CloseableHttpResponse response) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String inputLine;
        StringBuilder responseString = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            responseString.append(inputLine);
        }
        return responseString.toString();
    }

}
