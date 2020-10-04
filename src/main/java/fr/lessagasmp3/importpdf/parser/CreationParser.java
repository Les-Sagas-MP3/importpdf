package fr.lessagasmp3.importpdf.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Service
public class CreationParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreationParser.class);

    public Date parse(String creation) {
        Date creationDate = new Date();
        SimpleDateFormat formater;
        String[] splitCreation = creation.split(" ");
        switch (splitCreation.length) {
            case 3:
                formater = new SimpleDateFormat("d MMM yyyy", Locale.FRENCH);
                break;
            case 2:
                formater = new SimpleDateFormat("MMM yyyy", Locale.FRENCH);
                break;
            case 1:
                formater = new SimpleDateFormat("yyyy", Locale.FRENCH);
                break;
            case 6:
                creation = splitCreation[0] + " " + splitCreation[1] + " " + splitCreation[2];
                formater = new SimpleDateFormat("d MMM yyyy", Locale.FRENCH);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + creation);
        }
        try {
            creationDate = formater.parse(creation);
        } catch (ParseException e) {
            LOGGER.error("Cannote parse {} to date", creation, e);
        }
        return creationDate;
    }

}
