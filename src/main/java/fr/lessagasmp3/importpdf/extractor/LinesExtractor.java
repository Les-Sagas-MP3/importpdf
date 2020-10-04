package fr.lessagasmp3.importpdf.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LinesExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinesExtractor.class);

    public String extractLines(String lineParsed, String enterringTag, String[] nextTags, String[] lines, int lineNumber) {
        String line = lines[lineNumber];
        if (lineParsed == null && line.toUpperCase().startsWith(enterringTag)) {
            LOGGER.debug("{} recognized", enterringTag);
            StringBuilder separatedEntities = new StringBuilder();
            String[] lineSplitColon = line.split(": ");
            separatedEntities.append(lineSplitColon[1]);
            String nextLine = lines[lineNumber + 1].toUpperCase();
            boolean reachedNextTag = false;
            for (String nextTag : nextTags) {
                reachedNextTag |= nextLine.startsWith(nextTag);
            }
            while (!reachedNextTag) {
                lineNumber++;
                separatedEntities.append(lines[lineNumber]);
                LOGGER.debug(lines[lineNumber]);
                nextLine = lines[lineNumber + 1].toUpperCase();
                for (String nextTag : nextTags) {
                    reachedNextTag |= nextLine.startsWith(nextTag);
                }
            }
            lineParsed = separatedEntities.toString();
            LOGGER.debug("{} : {}", enterringTag, lineParsed);
        }
        return lineParsed;
    }

    public String extractMultilines(String lineParsed, String enterringTag, String[] nextTags, String[] lines, int lineNumber) {
        if (lineParsed == null && lines[lineNumber].toUpperCase().startsWith(enterringTag)) {
            LOGGER.debug("{} recognized", enterringTag);
            StringBuilder separatedEntities = new StringBuilder();
            String nextLine = lines[lineNumber + 1].toUpperCase();
            boolean reachedNextTag = lineNumber + 1 >= lines.length;
            for (String nextTag : nextTags) {
                reachedNextTag |= nextLine.startsWith(nextTag);
            }
            while (!reachedNextTag) {
                lineNumber++;
                separatedEntities.append(lines[lineNumber]);
                separatedEntities.append("\n");
                LOGGER.debug(lines[lineNumber]);
                if (lineNumber + 1 < lines.length) {
                    nextLine = lines[lineNumber + 1].toUpperCase();
                    for (String nextTag : nextTags) {
                        reachedNextTag |= nextLine.startsWith(nextTag);
                    }
                } else {
                    reachedNextTag = true;
                }
            }
            lineParsed = separatedEntities.toString();
            LOGGER.debug("{} : {}", enterringTag, lineParsed);
        }
        return lineParsed;
    }

}
