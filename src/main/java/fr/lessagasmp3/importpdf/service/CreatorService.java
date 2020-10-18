package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
import fr.lessagasmp3.core.model.CreatorModel;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreatorService extends HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreatorService.class);

    @Autowired
    private Gson gson;

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
        String json = executeRequest(new HttpGet(url));
        if(json != null) {
            return gson.fromJson(json, CreatorModel.class);
        }
        return null;
    }

    public CreatorModel create(CreatorModel author) {
        String url = coreUrl + "/api/authors";
        String body = gson.toJson(author);
        String json = executeRequest(new HttpPost(url), body);
        if(json != null) {
            return gson.fromJson(json, CreatorModel.class);
        }
        return null;
    }

    public void update(CreatorModel author) {
        String url = coreUrl + "/api/authors";
        String body = gson.toJson(author);
        executeRequest(new HttpPut(url), body);
    }

}
