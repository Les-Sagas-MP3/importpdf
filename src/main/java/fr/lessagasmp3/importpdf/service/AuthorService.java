package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.lessagasmp3.core.model.AuthorModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class AuthorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorService.class);

    @Autowired
    private HttpClientService httpClientService;

    @Value("${fr.lessagasmp3.core.url}")
    private String coreUrl;

    @Value("${fr.lessagasmp3.core.token}")
    private String token;

    public AuthorModel findByName(String name) {
        String url = coreUrl + "/api/authors?name=" + encodeValue(name);
        LOGGER.debug("GET " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .build();
        HttpResponse<String> response;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("response : " + response.body());
            Gson gson = new Gson();
            return gson.fromJson(response.body(), AuthorModel.class);
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public void create(AuthorModel author) {
        String url = coreUrl + "/api/authors";
        Gson gson = new Gson();
        String body = gson.toJson(author);
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("response : " + response.body());
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(AuthorModel author) {
        String url = coreUrl + "/api/authors";
        Gson gson = new Gson();
        String body = gson.toJson(author);
        LOGGER.debug("PUT " + url);
        LOGGER.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("response : " + response.body());
        } catch (IOException | InterruptedException e) {
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
