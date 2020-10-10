package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.core.entity.Anecdote;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class AnecdoteParser {

    public Set<Anecdote> parse(String anecdotes) {
        String[] paragraphs = anecdotes.split("\\.\n");
        Set<Anecdote> anecdotesSet = new LinkedHashSet<>();

        if(paragraphs.length == 1) {
            Anecdote anecdote = new Anecdote();
            anecdote.setAnecdote(anecdotes.replace("\n", ""));
            anecdotesSet.add(anecdote);
        } else {
            for (String paragraph : paragraphs) {
                Anecdote anecdote = new Anecdote();
                anecdote.setAnecdote(paragraph.replace("\n", "") + ".\n");
                anecdotesSet.add(anecdote);
            }
        }

        return anecdotesSet;
    }


}
