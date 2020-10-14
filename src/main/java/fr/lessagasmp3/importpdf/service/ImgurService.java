package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
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

import java.io.IOException;

@Service
public class ImgurService extends HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImgurService.class);

    @Value("${imgur.token}")
    private String token;

    @Autowired
    private Gson gson;

    public String createAlbum(String name) {
        String url = "https://api.imgur.com/3/album";
        LOGGER.debug("POST " + url);

        HttpEntity entity = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addPart("title", new StringBody("Les Sagas MP3 - " + name, ContentType.MULTIPART_FORM_DATA))
                .addPart("description", new StringBody(name, ContentType.MULTIPART_FORM_DATA))
                .build();

        String json = executeRequest(new HttpPost(url), entity);
        if(json != null) {
            return json;
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
