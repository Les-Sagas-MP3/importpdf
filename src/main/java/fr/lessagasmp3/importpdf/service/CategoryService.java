package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
import fr.lessagasmp3.core.model.CategoryModel;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class CategoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private HttpClientService httpClientService;

    @Value("${fr.lessagasmp3.core.url}")
    private String coreUrl;

    @Value("${fr.lessagasmp3.core.token}")
    private String token;

    public CategoryModel findByName(String name) {
        String url = coreUrl + "/api/categories?name=" + encodeValue(name);
        LOGGER.debug("GET " + url);
        String json = executeRequest(new HttpGet(url));
        if(json != null) {
            Gson gson = new Gson();
            return gson.fromJson(executeRequest(new HttpGet(url)), CategoryModel.class);
        }
        return null;
    }

    public void create(CategoryModel category) {
        String url = coreUrl + "/api/categories";
        Gson gson = new Gson();
        String body = gson.toJson(category);
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        executeRequest(new HttpPost(url), body);
    }

    public void update(CategoryModel category) {
        String url = coreUrl + "/api/categories";
        Gson gson = new Gson();
        String body = gson.toJson(category);
        LOGGER.debug("PUT " + url);
        LOGGER.debug("body : " + body);
        executeRequest(new HttpPut(url), body);
    }

    private String executeRequest(HttpRequestBase request) {
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + token);
        CloseableHttpResponse response;
        try (CloseableHttpClient httpClient = httpClientService.getHttpClient()) {
            response = httpClient.execute(request);
            String responseString = httpClientService.getStringResponse(response);
            LOGGER.debug("response : " + responseString);
            return responseString;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private void executeRequest(HttpEntityEnclosingRequestBase request, String body) {
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + token);
        CloseableHttpResponse response;
        try (CloseableHttpClient httpClient = httpClientService.getHttpClient()) {
            request.setEntity(new StringEntity(body));
            response = httpClient.execute(request);
            LOGGER.debug("response : " + httpClientService.getStringResponse(response));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

}
