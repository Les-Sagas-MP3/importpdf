package fr.lessagasmp3.importpdf.parser;

import org.springframework.stereotype.Service;

@Service
public class TextParser {

    public String parse(String multilineString) {

        String[] paragraphs = multilineString.split("\\.\n");

        if(paragraphs.length == 1) {
            return multilineString.replace("\n", "");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String paragraph : paragraphs) {
            stringBuilder.append(paragraph.replace("\n", ""));
            stringBuilder.append(".\n");
        }

        return stringBuilder.toString();
    }


}
