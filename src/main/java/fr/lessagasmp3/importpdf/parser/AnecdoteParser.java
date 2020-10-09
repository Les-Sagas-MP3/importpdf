package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.core.entity.Anecdote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class AnecdoteParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnecdoteParser.class);

    public Set<Anecdote> parse(String anecdotes) {
        String[] lines = anecdotes.split("\n");
        Set<Anecdote> anecdotesSet = new LinkedHashSet<>();
        return anecdotesSet;
    }


}
