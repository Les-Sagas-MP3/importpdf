package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.core.entity.Episode;
import fr.lessagasmp3.core.entity.Season;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class EpisodeParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpisodeParser.class);

    public Set<Season> parse(String episodes) {
        String[] lines = episodes.split("\n");
        Set<Season> seasons = new LinkedHashSet<>();
        Integer episodeNumber = 0;
        Season season = new Season();
        season.setNumber(1);
        for(int lineNumber = 0 ; lineNumber < lines.length ; lineNumber++) {

            String upperLine = lines[lineNumber].toUpperCase();
            if(upperLine.startsWith("SAISON")) {
                if(lineNumber > 0) {
                    seasons.add(season);
                }
                season = new Season();
                String seasonNumber = upperLine
                        .replace("SAISON ", "")
                        .replace(" ", "");
                season.setNumber(Integer.valueOf(seasonNumber));
                episodeNumber = 0;
                lineNumber++;
            }
            if(upperLine.startsWith("SÉRIE")) {
                if(lineNumber > 0) {
                    seasons.add(season);
                }
                season = new Season();
                String[] splitHyphen1 = lines[lineNumber].split("- ");
                String[] splitHyphen2 = lines[lineNumber].split("– ");
                String[] splitHyphen = new String[1];
                splitHyphen[0] = lines[lineNumber];
                if(splitHyphen1.length > 1) {
                    String seasonName = splitHyphen1[1];
                    splitHyphen = splitHyphen1;
                }
                if(splitHyphen2.length > 1) {
                    LOGGER.debug(lines[lineNumber]);
                    String seasonName = splitHyphen2[1];
                    splitHyphen = splitHyphen2;
                }
                String seasonNumber = splitHyphen[0].toUpperCase()
                        .replace("SÉRIE ", "")
                        .replace(" ", "");
                season.setNumber(Integer.valueOf(seasonNumber));
                episodeNumber = 0;
                lineNumber++;
            }

            String[] splitHyphen1 = lines[lineNumber].split("- ");
            String[] splitHyphen2 = lines[lineNumber].split("– ");
            if(splitHyphen1.length > 1 || splitHyphen2.length > 1) {
                String[] splitHyphen = splitHyphen(splitHyphen1, splitHyphen2);
                if(splitHyphen == null && lineNumber > 0) {
                    String multiline = lines[lineNumber-1] + lines[lineNumber];
                    splitHyphen1 = lines[lineNumber].split("- ");
                    splitHyphen2 = lines[lineNumber].split("– ");
                    splitHyphen = splitHyphen(splitHyphen1, splitHyphen2);
                    if(splitHyphen != null) {
                        Episode episode = new Episode();
                        String episodeDisplayedNumber = splitHyphen[0];
                        String[] splitParenthesis = splitHyphen[1].split("\\(");
                        if(splitHyphen[1].split("\\(").length > 1) {
                            episode.setTitle(splitParenthesis[0]);
                            episode.setInfos(splitParenthesis[1].replace(")", ""));
                        } else {
                            episode.setTitle(splitHyphen[1]);
                        }
                        episode.setNumber(episodeNumber);
                        episodeNumber++;
                        season.getEpisodes().add(episode);
                    }
                } else if(splitHyphen != null) {
                    Episode episode = new Episode();
                    String episodeDisplayedNumber = splitHyphen[0];
                    String[] splitParenthesis = splitHyphen[1].split("\\(");
                    if(splitHyphen[1].split("\\(").length > 1) {
                        episode.setTitle(splitParenthesis[0]);
                        episode.setInfos(splitParenthesis[1].replace(")", ""));
                    } else {
                        episode.setTitle(splitHyphen[1]);
                    }
                    episode.setNumber(episodeNumber);
                    episodeNumber++;
                    season.getEpisodes().add(episode);
                }

                LOGGER.debug(lines[lineNumber]);
            }
        }

        seasons.add(season);

        return seasons;
    }

    private String[] splitHyphen(String[] splitHyphen1, String[] splitHyphen2) {
        String[] splitHyphen = null;
        if(splitHyphen1.length > 1) {
            if(isNumeric(splitHyphen1[0])) {
                splitHyphen = splitHyphen1;
            }
        }
        if(splitHyphen2.length > 1) {
            if(isNumeric(splitHyphen2[0])) {
                splitHyphen = splitHyphen2;
            }
        }
        return splitHyphen;
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        String strNumSimplified = strNum
                .replace(" ", "")
                .replace(",", "")
                .replace("bis", "");
        try {
            double d = Double.parseDouble(strNumSimplified);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
