package fr.lessagasmp3.importpdf.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DistributionExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionExtractor.class);

    public String extract(String lineParsed, String[] lines, int lineNumber) {
        String line = lines[lineNumber];
        if (lineParsed == null && !(line.startsWith("http://") || line.startsWith("https://")) && !lines[lineNumber + 1].toUpperCase().contains("SYNOPSIS")) {
            LOGGER.debug("DISTRIBUTION recognized");
            StringBuilder separatedEntities = new StringBuilder();
            separatedEntities.append(line);
            separatedEntities.append("\n");
            String nextLine = lines[lineNumber + 2].toUpperCase();
            boolean reachedNextTag = nextLine.contains("SYNOPSIS");
            while (!reachedNextTag) {
                lineNumber++;
                line = lines[lineNumber];
                separatedEntities.append(line);
                separatedEntities.append("\n");
                LOGGER.debug(lines[lineNumber]);
                nextLine = lines[lineNumber + 2].toUpperCase();
                reachedNextTag = nextLine.contains("SYNOPSIS");
            }
            lineParsed = separatedEntities.toString();
            LOGGER.debug("DISTRIBUTION : {}", lineParsed);
        }
        return lineParsed;
    }



}
