package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
import fr.lessagasmp3.core.model.SeasonModel;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeasonService extends HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeasonService.class);

    @Autowired
    private Gson gson;

    public SeasonModel findOrCreate(Integer number, Long sagaId) {
        SeasonModel season = findByNumberAndSagaId(number, sagaId);
        if (season == null) {
            season = new SeasonModel();
            season.setNumber(number);
            season.setSagaRef(sagaId);
            season = create(season);
            if(season != null) {
                LOGGER.debug("Season {} created", season);
            } else {
                LOGGER.error("Season {} not created", number);
            }
        } else {
            LOGGER.debug("Season already exists : {}", season);
        }
        return season;
    }

    public SeasonModel findByNumberAndSagaId(Integer number, Long sagaId) {
        String url = coreUrl + "/season?number=" + number + "&sagaId=" + sagaId;
        String json = executeRequest(new HttpGet(url));
        if(json != null) {
            return gson.fromJson(json, SeasonModel.class);
        }
        return null;
    }

    public SeasonModel create(SeasonModel model) {
        String url = coreUrl + "/season";
        String body = gson.toJson(model);
        String json = executeRequest(new HttpPost(url), body);
        if(json != null) {
            return gson.fromJson(json, SeasonModel.class);
        }
        return null;
    }

    public void update(SeasonModel model) {
        String url = coreUrl + "/season";
        String body = gson.toJson(model);
        executeRequest(new HttpPut(url), body);
    }

}
