package fr.lessagasmp3.importpdf.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TitleExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TitleExtractor.class);

    public String extract(String lineParsed, String[] lines, int lineNumber, String filename, Boolean[] needsManualCheck) {
        if (lineParsed == null && lines[lineNumber + 1].toUpperCase().contains("SYNOPSIS")) {
            LOGGER.debug("TITLE recognized");
            lineParsed = lines[lineNumber];
            LOGGER.debug("TITLE : {}", lineParsed);
            if (filename.length() > 34) {
                LOGGER.warn("Filename length > 34, please verify title and distribution");
                LOGGER.warn("TITLE : {}", lineParsed);
                LOGGER.warn("FILENAME : {}", filename);
                needsManualCheck[0] = true;
            }
            lineParsed = lineParsed.replace("–", "-");
        }
        return lineParsed;
    }


}
