package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ImgurService extends HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImgurService.class);

    @Value("${imgur.clientId}")
    private String clientId;

    @Value("${imgur.token}")
    private String token;

    @Autowired
    private Gson gson;

    public String createAlbum() {
        String url = "https://api.imgur.com/3/album";
        LOGGER.debug("POST " + url);

        HttpEntity entity = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addPart("title", new StringBody("Les Sagas MP3 - Resources", ContentType.MULTIPART_FORM_DATA))
                .addPart("description", new StringBody("Resources", ContentType.MULTIPART_FORM_DATA))
                .build();

        String json = executeRequest(new HttpPost(url), entity);
        if(json != null) {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            if(response.has("data")) {
                if(response.getAsJsonObject("data").has("id")) {
                    return response.getAsJsonObject("data").get("id").getAsString();
                }
            }
        }
        return null;
    }

    public String upload(File file, String albumHash, String title) {
        String url = "https://api.imgur.com/3/upload";
        LOGGER.debug("POST " + url);

        HttpEntity entity = null;
        try {
            entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addBinaryBody("image", file, ContentType.parse(Files.probeContentType(Path.of(file.getPath()))), file.getName())
                    .addPart("album", new StringBody(albumHash, ContentType.MULTIPART_FORM_DATA))
                    .addPart("title", new StringBody(title, ContentType.MULTIPART_FORM_DATA))
                    .build();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        String json = executeRequest(new HttpPost(url), entity);
        if(json != null) {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            if(response.has("data")) {
                if(response.getAsJsonObject("data").has("link")) {
                    return response.getAsJsonObject("data").get("link").getAsString();
                }
            }
        }
        return null;
    }

    protected String executeRequest(HttpEntityEnclosingRequestBase request, HttpEntity entity) {
        request.addHeader("Authorization", "Bearer " + token);
        CloseableHttpResponse response;
        try (CloseableHttpClient httpClient = getHttpClient()) {
            request.setEntity(entity);
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

}
