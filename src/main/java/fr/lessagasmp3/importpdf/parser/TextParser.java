package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.importpdf.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TextParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextParser.class);

    @Autowired
    private AuthorService authorService;

    public String parse(String multilineString) {
        // TODO : Convert multiple lines in paragraphs
        return multilineString;
    }


}
