package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
import fr.lessagasmp3.core.model.CreatorModel;
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
public class AuthorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorService.class);

    @Autowired
    private HttpClientService httpClientService;

    @Value("${fr.lessagasmp3.core.url}")
    private String coreUrl;

    @Value("${fr.lessagasmp3.core.token}")
    private String token;

    public CreatorModel findOrCreate(String name) {
        CreatorModel creator = findByName(name);
        if (creator == null) {
            creator = new CreatorModel();
            creator.setName(name);
            creator.setNbSagas(1);
            creator = create(creator);
            if(creator != null) {
                LOGGER.debug("Creator {} created", creator);
            } else {
                LOGGER.error("Creator {} not created", name);
            }
        } else {
            LOGGER.debug("Creator already exists : {}", creator);
        }
        return creator;
    }

    public CreatorModel findByName(String name) {
        String url = coreUrl + "/api/authors?name=" + encodeValue(name);
        LOGGER.debug("GET " + url);
        String json = executeRequest(new HttpGet(url));
        if(json != null) {
            Gson gson = new Gson();
            return gson.fromJson(executeRequest(new HttpGet(url)), CreatorModel.class);
        }
        return null;
    }

    public CreatorModel create(CreatorModel author) {
        String url = coreUrl + "/api/authors";
        Gson gson = new Gson();
        String body = gson.toJson(author);
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        String json = executeRequest(new HttpPost(url), body);
        if(json != null) {
            return gson.fromJson(json, CreatorModel.class);
        }
        return null;
    }

    public void update(CreatorModel author) {
        String url = coreUrl + "/api/authors";
        Gson gson = new Gson();
        String body = gson.toJson(author);
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

    private String executeRequest(HttpEntityEnclosingRequestBase request, String body) {
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + token);
        CloseableHttpResponse response;
        try (CloseableHttpClient httpClient = httpClientService.getHttpClient()) {
            request.setEntity(new StringEntity(body));
            response = httpClient.execute(request);
            String responseString = httpClientService.getStringResponse(response);
            if(response.getStatusLine().getStatusCode() == 200) {
                LOGGER.debug("response : " + responseString);
                return responseString;
            } else {
                LOGGER.error("response : " + responseString);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

}
