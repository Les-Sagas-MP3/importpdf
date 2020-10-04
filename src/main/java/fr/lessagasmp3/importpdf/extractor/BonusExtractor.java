package fr.lessagasmp3.importpdf.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BonusExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BonusExtractor.class);

    public String extract(String lineParsed, String[] lines, int lineNumber) {
        String enterringTag = "BONUS";
        String line = lines[lineNumber];
        if (lineParsed == null && line.toUpperCase().startsWith(enterringTag)) {
            LOGGER.debug("{} recognized", enterringTag);
            StringBuilder separatedEntities = new StringBuilder();
            String[] lineSplitColon = line.split(": ");
            separatedEntities.append(lineSplitColon[1]);
            lineParsed = separatedEntities.toString();
            LOGGER.debug("{} : {}", enterringTag, lineParsed);
        }
        return lineParsed;
    }


}
