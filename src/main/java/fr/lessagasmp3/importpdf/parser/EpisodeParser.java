package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.core.model.EpisodeModel;
import fr.lessagasmp3.core.model.SeasonModel;
import fr.lessagasmp3.importpdf.extractor.LinesExtractor;
import fr.lessagasmp3.importpdf.service.EpisodeService;
import fr.lessagasmp3.importpdf.service.SeasonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EpisodeParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpisodeParser.class);

    @Autowired
    private EpisodeService episodeService;

    @Autowired
    private SeasonService seasonService;

    public void parse(String episodes, Long sagaId) {
        String[] lines = episodes.split("\n");
        int episodeNumber = 1;
        SeasonModel season = seasonService.findOrCreate(1, sagaId);
        for(int lineNumber = 0 ; lineNumber < lines.length ; lineNumber++) {

            String upperLine = lines[lineNumber].toUpperCase();
            if(upperLine.startsWith("SAISON") || upperLine.startsWith("SÉRIE")) {
                String[] splitHyphen1 = lines[lineNumber].split("- ");
                String[] splitHyphen2 = lines[lineNumber].split("– ");
                String[] splitHyphen = new String[1];
                splitHyphen[0] = lines[lineNumber];
                String seasonName = "";
                if(splitHyphen1.length > 1) {
                    seasonName =splitHyphen1[1];
                    splitHyphen = splitHyphen1;
                }
                if(splitHyphen2.length > 1) {
                    LOGGER.debug(lines[lineNumber]);
                    seasonName =splitHyphen2[1];
                    splitHyphen = splitHyphen2;
                }
                String seasonNumber = splitHyphen[0].toUpperCase()
                        .replace("SAISON ", "")
                        .replace("SÉRIE ", "")
                        .replace(" ", "");
                season = seasonService.findOrCreate(Integer.valueOf(seasonNumber), sagaId);
                season.setName(LinesExtractor.removeLastSpaces(seasonName));
                seasonService.update(season);
                episodeNumber = 1;
                lineNumber++;
            }

            String[] splitHyphen1 = lines[lineNumber].split("- ");
            String[] splitHyphen2 = lines[lineNumber].split("– ");
            if(splitHyphen1.length > 1 || splitHyphen2.length > 1) {
                String[] splitHyphen = splitHyphen(splitHyphen1, splitHyphen2);
                if(splitHyphen == null && lineNumber > 0) {
                    String multiline = lines[lineNumber-1] + lines[lineNumber];
                    splitHyphen1 = multiline.split("- ");
                    splitHyphen2 = multiline.split("– ");
                    splitHyphen = splitHyphen(splitHyphen1, splitHyphen2);
                    if(splitHyphen != null) {
                        EpisodeModel episode = episodeService.findOrCreate(episodeNumber, season.getId());
                        episode.setDisplayedNumber(splitHyphen[0]);
                        String[] splitParenthesis = splitHyphen[1].split("\\(");
                        if(splitHyphen[1].split("\\(").length > 1) {
                            episode.setTitle(LinesExtractor.removeLastSpaces(splitParenthesis[0]));
                            episode.setInfos(splitParenthesis[1].replace(")", ""));
                        } else {
                            episode.setTitle(LinesExtractor.removeLastSpaces(splitHyphen[1]));
                        }
                        episodeService.update(episode);
                        episodeNumber++;
                    }
                } else if(splitHyphen != null) {
                    EpisodeModel episode = episodeService.findOrCreate(episodeNumber, season.getId());
                    episode.setDisplayedNumber(splitHyphen[0]);
                    String[] splitParenthesis = splitHyphen[1].split("\\(");
                    if(splitHyphen[1].split("\\(").length > 1) {
                        episode.setTitle(LinesExtractor.removeLastSpaces(splitParenthesis[0]));
                        episode.setInfos(splitParenthesis[1].replace(")", ""));
                    } else {
                        episode.setTitle(LinesExtractor.removeLastSpaces(splitHyphen[1]));
                    }
                    episodeService.update(episode);
                    episodeNumber++;
                }
                LOGGER.debug(lines[lineNumber]);
            }
        }
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
