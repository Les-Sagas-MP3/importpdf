package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
import fr.lessagasmp3.core.model.AnecdoteModel;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnecdoteService extends HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnecdoteService.class);

    @Autowired
    private Gson gson;

    public AnecdoteModel findOrCreate(String content, Long sagaId) {
        AnecdoteModel anecdote = findByAnecdoteAndSagaId(content, sagaId);
        if (anecdote == null) {
            anecdote = new AnecdoteModel();
            anecdote.setAnecdote(content);
            anecdote.setSagaRef(sagaId);
            anecdote = create(anecdote);
            if(anecdote != null) {
                LOGGER.debug("Anecdote {} created", anecdote);
            } else {
                LOGGER.error("Anecdote {} not created", content);
            }
        } else {
            LOGGER.debug("Anecdote already exists : {}", anecdote);
        }
        return anecdote;
    }

    public AnecdoteModel findByAnecdoteAndSagaId(String content, Long sagaId) {
        String url = coreUrl + "/anecdote?content=" + encodeValue(content) + "&sagaId=" + sagaId;
        String json = executeRequest(new HttpGet(url));
        if(json != null) {
            return gson.fromJson(json, AnecdoteModel.class);
        }
        return null;
    }

    public AnecdoteModel create(AnecdoteModel model) {
        String url = coreUrl + "/anecdote";
        String body = gson.toJson(model);
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        String json = executeRequest(new HttpPost(url), body);
        if(json != null) {
            return gson.fromJson(json, AnecdoteModel.class);
        }
        return null;
    }

    public void update(AnecdoteModel model) {
        String url = coreUrl + "/anecdote";
        String body = gson.toJson(model);
        LOGGER.debug("PUT " + url);
        LOGGER.debug("body : " + body);
        executeRequest(new HttpPut(url), body);
    }

}
