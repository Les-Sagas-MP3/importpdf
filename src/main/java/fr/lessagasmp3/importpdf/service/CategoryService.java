package fr.lessagasmp3.importpdf.service;

import fr.lessagasmp3.core.model.CategoryModel;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    public CategoryModel findByName(String name) {
        String url = coreUrl + "/api/categories?name=" + encodeValue(name);
        LOGGER.debug("GET " + url);
        String json = executeRequest(new HttpGet(url));
        if(json != null) {
            return gson.fromJson(executeRequest(new HttpGet(url)), CategoryModel.class);
        }
        return null;
    }

    public void create(CategoryModel category) {
        String url = coreUrl + "/api/categories";
        String body = gson.toJson(category);
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        executeRequest(new HttpPost(url), body);
    }

    public void update(CategoryModel category) {
        String url = coreUrl + "/api/categories";
        String body = gson.toJson(category);
        LOGGER.debug("PUT " + url);
        LOGGER.debug("body : " + body);
        executeRequest(new HttpPut(url), body);
    }

}
