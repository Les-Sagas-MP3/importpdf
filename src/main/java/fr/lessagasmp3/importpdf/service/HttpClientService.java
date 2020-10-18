package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
import fr.lessagasmp3.core.constant.Strings;
import fr.lessagasmp3.importpdf.extractor.LinesExtractor;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientService.class);

    private static final String HTTP_PROXY = System.getenv("HTTP_PROXY");

    @Autowired
    protected Gson gson;

    @Value("${fr.lessagasmp3.core.url}")
    protected String coreUrl;

    @Value("${fr.lessagasmp3.core.token}")
    protected String token;

    protected CloseableHttpClient getHttpClient() {

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

    protected String executeRequest(HttpRequestBase request) {
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + token);
        CloseableHttpResponse response;
        try (CloseableHttpClient httpClient = getHttpClient()) {
            LOGGER.debug("{} : {}", request.getMethod(), request.getURI());
            response = httpClient.execute(request);
            String responseString = getStringResponse(response);
            if(responseString != null && !responseString.isEmpty()) {
                LOGGER.debug("response : " + responseString);
            } else {
                LOGGER.debug("response : <empty>");
            }
            return responseString;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    protected String executeRequest(HttpEntityEnclosingRequestBase request, String body) {
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + token);
        CloseableHttpResponse response;
        try (CloseableHttpClient httpClient = getHttpClient()) {
            StringEntity e = new StringEntity(body, StandardCharsets.UTF_8);
            e.setContentEncoding("UTF-8");
            e.setContentType("application/json");
            LOGGER.debug("{} : {}", request.getMethod(), request.getURI());
            LOGGER.debug("BODY : {}", body);
            String text = new BufferedReader(new InputStreamReader(e.getContent(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            LOGGER.debug("ENTITY : {}", text);
            request.setEntity(e);
            response = httpClient.execute(request);
            String responseString = getStringResponse(response);
            if(response.getStatusLine().getStatusCode() == 200) {
                LOGGER.debug("response : " + responseString);
                return responseString;
            } else {
                LOGGER.error("response : " + responseString);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public String getStringResponse(CloseableHttpResponse response) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder responseString = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            responseString.append(inputLine);
        }
        return LinesExtractor.convertToUtf8(responseString.toString());
    }

    protected static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

}
