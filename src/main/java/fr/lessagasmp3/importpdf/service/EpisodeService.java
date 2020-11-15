package fr.lessagasmp3.importpdf.service;

import com.google.gson.Gson;
import fr.lessagasmp3.core.model.EpisodeModel;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EpisodeService extends HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpisodeService.class);

    @Autowired
    private Gson gson;

    public EpisodeModel findOrCreate(Integer number, Long seasonId) {
        EpisodeModel episode = findByNumberAndSeasonId(number, seasonId);
        if (episode == null) {
            episode = new EpisodeModel();
            episode.setNumber(number);
            episode.setSeasonRef(seasonId);
            episode = create(episode);
            if(episode != null) {
                LOGGER.debug("Episode {} created", episode);
            } else {
                LOGGER.error("Episode {} not created", number);
            }
        } else {
            LOGGER.debug("Episode already exists : {}", episode);
        }
        return episode;
    }

    public EpisodeModel findByNumberAndSeasonId(Integer number, Long seasonId) {
        String url = coreUrl + "/episode?number=" + number + "&seasonId=" + seasonId;
        String json = executeRequest(new HttpGet(url));
        if(json != null) {
            return gson.fromJson(json, EpisodeModel.class);
        }
        return null;
    }

    public EpisodeModel create(EpisodeModel model) {
        String url = coreUrl + "/episode";
        String body = gson.toJson(model);
        String json = executeRequest(new HttpPost(url), body);
        if(json != null) {
            return gson.fromJson(json, EpisodeModel.class);
        }
        return null;
    }

    public void update(EpisodeModel model) {
        String url = coreUrl + "/episode";
        String body = gson.toJson(model);
        executeRequest(new HttpPut(url), body);
    }

}
