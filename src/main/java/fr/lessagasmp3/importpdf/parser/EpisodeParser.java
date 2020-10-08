package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.core.entity.Season;
import fr.lessagasmp3.importpdf.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class EpisodeParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpisodeParser.class);

    @Autowired
    private AuthorService authorService;

    public Set<Season> parse(String episodes) {
        String[] lines = episodes.split("\n");
        Set<Season> seasons = new LinkedHashSet<>();
        return seasons;
    }


}
