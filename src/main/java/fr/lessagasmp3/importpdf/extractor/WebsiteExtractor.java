package fr.lessagasmp3.importpdf.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WebsiteExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteExtractor.class);

    public String extract(String lineParsed, String[] lines, int lineNumber) {
        String line = lines[lineNumber];
        if (lineParsed == null && (line.startsWith("http://") || line.startsWith("https://"))) {
            LOGGER.debug("WEBSITE recognized");
            StringBuilder separatedEntities = new StringBuilder();
            separatedEntities.append(line);
            String nextLine = lines[lineNumber + 1].toUpperCase();
            boolean reachedNextPart = nextLine.indexOf(" ") != nextLine.length() - 1 && nextLine.contains(" ");
            while (!reachedNextPart) {
                lineNumber++;
                separatedEntities.append(lines[lineNumber]);
                LOGGER.debug(lines[lineNumber]);
                nextLine = lines[lineNumber + 1].toUpperCase();
                reachedNextPart = nextLine.contains(" ");
            }
            lineParsed = separatedEntities.toString();
            LOGGER.debug("WEBSITE : {}", lineParsed);
        }
        return lineParsed;
    }


}
