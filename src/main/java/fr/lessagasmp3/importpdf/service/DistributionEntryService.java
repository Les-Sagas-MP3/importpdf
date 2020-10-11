package fr.lessagasmp3.importpdf.service;

import fr.lessagasmp3.core.model.DistributionEntryModel;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DistributionEntryService extends HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionEntryService.class);

    public DistributionEntryModel findOrCreate(Long actorId, Long sagaId, String roles) {
        DistributionEntryModel distributionEntry = findByActorIdAndSagaIdAndRoles(actorId, sagaId, roles);
        if (distributionEntry == null) {
            distributionEntry = new DistributionEntryModel();
            distributionEntry.setActorRef(actorId);
            distributionEntry.setSagaRef(sagaId);
            distributionEntry.setRoles(roles);
            distributionEntry = create(distributionEntry);
            if(distributionEntry != null) {
                LOGGER.debug("Distribytion Entry {} created", distributionEntry);
            } else {
                LOGGER.error("Distribytion Entry {}:{} not created", actorId, roles);
            }
        } else {
            LOGGER.debug("Distribytion Entry already exists : {}", distributionEntry);
        }
        return distributionEntry;
    }

    public DistributionEntryModel findByActorIdAndSagaIdAndRoles(Long actorId, Long sagaId, String roles) {
        String url = coreUrl + "/api/distribution?actorId=" + actorId + "&sagaId=" + sagaId + "&roles=" + encodeValue(roles);
        LOGGER.debug("GET " + url);
        String json = executeRequest(new HttpGet(url));
        if(json != null) {
            return gson.fromJson(executeRequest(new HttpGet(url)), DistributionEntryModel.class);
        }
        return null;
    }

    public DistributionEntryModel create(DistributionEntryModel distributionEntry) {
        String url = coreUrl + "/api/distribution";
        String body = gson.toJson(distributionEntry);
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        String json = executeRequest(new HttpPost(url), body);
        if(json != null) {
            return gson.fromJson(json, DistributionEntryModel.class);
        }
        return null;
    }

    public void update(DistributionEntryModel distributionEntry) {
        String url = coreUrl + "/api/distribution";
        String body = gson.toJson(distributionEntry);
        LOGGER.debug("PUT " + url);
        LOGGER.debug("body : " + body);
        executeRequest(new HttpPut(url), body);
    }


}
