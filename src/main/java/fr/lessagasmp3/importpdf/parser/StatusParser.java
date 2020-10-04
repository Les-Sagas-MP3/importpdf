package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.core.constant.SagaStatus;
import org.springframework.stereotype.Service;

@Service
public class StatusParser {

    public SagaStatus parse(String statusString) {
        return switch (statusString.toUpperCase()) {
            case "TERMINÉE" -> SagaStatus.FINISHED;
            default -> SagaStatus.IN_PROGRESS;
        };
    }


}
