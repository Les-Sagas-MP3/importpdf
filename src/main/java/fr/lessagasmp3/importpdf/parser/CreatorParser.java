package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.core.model.CreatorModel;
import fr.lessagasmp3.importpdf.service.CreatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class CreatorParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreatorParser.class);

    @Autowired
    private CreatorService creatorService;

    public Set<CreatorModel> parse(String creatorsString) {
        Set<CreatorModel> creators = new LinkedHashSet<>();
        creatorsString = creatorsString.replace(" & ", "|")
                .replace(" et ", "|")
                .replace(", ", "|")
                .replace("| ", "|")
                .replace(" |", "|");
        String[] splitAuthors = creatorsString.split("\\|");
        for (String authorStr : splitAuthors) {
            CreatorModel creator = creatorService.findByName(authorStr);
            if (creator == null) {
                creator = new CreatorModel();
                creator.setName(authorStr);
                creator = creatorService.create(creator);
                LOGGER.debug("Creator {} created", creator.getName());
            } else {
                LOGGER.debug("Creator already exists : ID={} NAME={}", creator.getId(), creator.getName());
            }
            creators.add(creator);
        }
        return creators;
    }


}
