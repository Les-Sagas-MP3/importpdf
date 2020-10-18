package fr.lessagasmp3.importpdf.service;

import fr.lessagasmp3.core.model.SagaModel;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SagaService extends HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaService.class);

    public SagaModel findOrCreate(String title) {
        SagaModel saga = findByTitle(title);
        if (saga == null) {
            saga = new SagaModel();
            saga.setTitle(title);
            saga = create(saga);
            if(saga != null) {
                LOGGER.debug("Creator {} created", saga);
            } else {
                LOGGER.error("Creator \"{}\" not created", title);
            }
        } else {
            LOGGER.debug("Creator already exists : {}", saga);
        }
        return saga;
    }

    public SagaModel findByTitle(String title) {
        String url = coreUrl + "/api/saga?title=" + encodeValue(title);
        String json = executeRequest(new HttpGet(url));
        if(json != null) {
            return gson.fromJson(json, SagaModel.class);
        }
        return null;
    }

    public SagaModel create(SagaModel saga) {
        String url = coreUrl + "/api/saga";
        String body = gson.toJson(saga);
        String json = executeRequest(new HttpPost(url), body);
        if(json != null) {
            return gson.fromJson(json, SagaModel.class);
        }
        return null;
    }

    public void update(SagaModel saga) {
        String url = coreUrl + "/api/saga";
        String body = gson.toJson(saga);
        LOGGER.debug("PUT " + url);
        LOGGER.debug("body : " + body);
        executeRequest(new HttpPut(url), body);
    }

    public void addAuthor(Long id, Long authorId) {
        String url = coreUrl + "/api/saga?id=" + id + "&authorId=" + authorId;
        executeRequest(new HttpPost(url), "");
    }

    public void addComposer(Long id, Long composerId) {
        String url = coreUrl + "/api/saga?id=" + id + "&composerId=" + composerId;
        executeRequest(new HttpPost(url), "");
    }

    public void addCategory(Long id, Long categoryId) {
        String url = coreUrl + "/api/saga?id=" + id + "&categoryId=" + categoryId;
        executeRequest(new HttpPost(url), "");
    }
}
