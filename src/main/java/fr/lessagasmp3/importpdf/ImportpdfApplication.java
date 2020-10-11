package fr.lessagasmp3.importpdf;

import fr.lessagasmp3.core.entity.Anecdote;
import fr.lessagasmp3.core.entity.DistributionEntry;
import fr.lessagasmp3.core.entity.Season;
import fr.lessagasmp3.core.model.CategoryModel;
import fr.lessagasmp3.core.model.CreatorModel;
import fr.lessagasmp3.core.model.SagaModel;
import fr.lessagasmp3.importpdf.extractor.*;
import fr.lessagasmp3.importpdf.parser.*;
import fr.lessagasmp3.importpdf.service.SagaService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ImportpdfApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportpdfApplication.class);

    @Autowired
    private ConfigurableApplicationContext ctx;

    @Autowired
    private BonusExtractor bonusExtractor;

    @Autowired
    private DistributionExtractor distributionExtractor;

    @Autowired
    private LinesExtractor linesExtractor;

    @Autowired
    private TitleExtractor titleExtractor;

    @Autowired
    private WebsiteExtractor websiteExtractor;

    @Autowired
    private AnecdoteParser anecdoteParser;

    @Autowired
    private CreationParser creationParser;

    @Autowired
    private CreatorParser creatorParser;

    @Autowired
    private CategoryParser categoryParser;

    @Autowired
    private DistributionParser distributionParser;

    @Autowired
    private DurationParser durationParser;

    @Autowired
    private EpisodeParser episodeParser;

    @Autowired
    private StatusParser statusParser;

    @Autowired
    private TextParser textParser;

    @Autowired
    private SagaService sagaService;

    @Value("${fr.lessagasmp3.importpdf.root.folder}")
    private String rootFolderPath;

    @Value("${fr.lessagasmp3.importpdf.ignoreManualCheck}")
    private Boolean ignoreManualCheck;

    public static void main(String[] args) {
        SpringApplication.run(ImportpdfApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        LOGGER.info("Starting import");

        File rootFolder = new File(rootFolderPath);
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            LOGGER.error("The path {} does not exist or is not a directory", rootFolderPath);
            throw new IllegalArgumentException();
        }
        LOGGER.info("{} : OK", rootFolderPath);

        String pdfsFolderPath = rootFolderPath + File.separator + "pdfs";
        File pdfsFolder = new File(pdfsFolderPath);
        if (!pdfsFolder.exists() || !pdfsFolder.isDirectory()) {
            LOGGER.error("The path {} does not exist or is not a directory", pdfsFolderPath);
            throw new IllegalArgumentException();
        }
        LOGGER.info("{} : OK", pdfsFolderPath);

        String imagesFolderPath = rootFolderPath + File.separator + "images";
        File imagesFolder = new File(pdfsFolderPath);
        if (!imagesFolder.exists() || !imagesFolder.isDirectory()) {
            LOGGER.error("The path {} does not exist or is not a directory", imagesFolderPath);
            throw new IllegalArgumentException();
        }
        LOGGER.info("{} : OK", imagesFolderPath);

        String[] contents = pdfsFolder.list();
        if (contents == null) {
            LOGGER.error("No pdf was detected");
            throw new IllegalArgumentException();
        }

		for(int i = 0 ; i < 1 ; i++) {
			String content = "Donjon de Naheulbeuk.pdf";
			//String content = "Dieu en peignoir (le).pdf";
			//String content = "Crash  La revanche.pdf";
			//String content = "Ⅲème Légion.pdf";
			parseFile(pdfsFolderPath, content);
		}
/*
        for (String content : contents) {
            parseFile(pdfsFolderPath, content);
        }
*/
        ctx.close();

    }

    private void parseFile(String folderPath, String content) {

        // Output data
        String authors;
        String music;
        String origin;
        String kind;
        String style;
        String status;
        String creation;
        String duration;
        String bonus;
        String website;
        String distribution;
        String title;
        String synopsis;
        String episodes;
        String anecdotes;
        String genese;
        String recompenses;
        Boolean[] needsManualCheck;

        String pdfPath = folderPath + File.separator + content;
        LOGGER.info("File : {}", content);
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {

            authors = null;
            music = null;
            origin = null;
            kind = null;
            style = null;
            status = null;
            creation = null;
            duration = null;
            bonus = null;
            website = null;
            distribution = null;
            title = null;
            synopsis = null;
            episodes = null;
            genese = null;
            anecdotes = null;
            recompenses = null;
            needsManualCheck = new Boolean[]{Boolean.FALSE};

            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                PDFTextStripper tStripper = new PDFTextStripper();
                String pdfFileInText = tStripper.getText(document);

                LOGGER.info("Parsing PDF");
                //split by whitespace
                String[] lines = pdfFileInText.split("\\r?\\n");
                for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
                    String line = lines[lineNumber];
                    LOGGER.debug("Analyzing line {} : {}", lineNumber, line);

                    // Table on left
                    String[] lineSplitColon = line.split(": ");
                    if (lineSplitColon.length >= 2) {

                        // Creator
                        String[] nextTagsAuthor = {"MUSIQUE", "ORIGIN"};
                        authors = linesExtractor.extractLines(authors, "AUTEUR", nextTagsAuthor, lines, lineNumber);

                        // Music
                        String[] nextTagsMusic = {"ORIGIN"};
                        music = linesExtractor.extractLines(music, "MUSIQUE", nextTagsMusic, lines, lineNumber);

                        // Origin
                        String[] nextTagOrigin = {"GENRE"};
                        origin = linesExtractor.extractLines(origin, "ORIGIN", nextTagOrigin, lines, lineNumber);

                        // Kind
                        String[] nextTagKind = {"STYLE"};
                        kind = linesExtractor.extractLines(kind, "GENRE", nextTagKind, lines, lineNumber);

                        // Style
                        String[] nextTagStyle = {"CRÉATION", "STATUT"};
                        style = linesExtractor.extractLines(style, "STYLE", nextTagStyle, lines, lineNumber);

                        // Status
                        String[] nextTagStatus = {"CRÉATION", "SAISON", "SÉRIE", "ARC", "1ER SÉRIE", "BLOC 1", "CYCLE 1", "OPUS 1"};
                        status = linesExtractor.extractLines(status, "STATUT", nextTagStatus, lines, lineNumber);

                        // Creation
                        String[] nextTagCreation = {"STATUT", "SAISON", "SÉRIE", "ARC", "1ER SÉRIE", "BLOC 1", "CYCLE 1", "OPUS 1"};
                        creation = linesExtractor.extractLines(creation, "CRÉATION", nextTagCreation, lines, lineNumber);

                        // Duration
                        String[] nextTagDuration = {"BONUS"};
                        duration = linesExtractor.extractLines(duration, "DURÉE", nextTagDuration, lines, lineNumber);

                        // Bonus
                        bonus = bonusExtractor.extract(bonus, lines, lineNumber);
                    }

                    // Website
                    website = websiteExtractor.extract(website, lines, lineNumber);

                    // Distribution
                    if (bonus != null && title == null && !line.contains("Bonus : ") && (line.split(" - ").length >= 2 || line.split(" : ").length >= 2)) {
                        distribution = distributionExtractor.extract(distribution, lines, lineNumber);
                    }

                    // Title
                    title = titleExtractor.extract(title, lines, lineNumber, content, needsManualCheck);

                    // Synopsis
                    String[] nextTagsSynopsis = {"ÉPISODE", "ANECDOTE", "GENÈSE"};
                    synopsis = linesExtractor.extractMultilines(synopsis, "SYNOPSIS", nextTagsSynopsis, lines, lineNumber);

                    // Episodes
                    String[] nextTagsEpisodes = {"ANECDOTE", "GENÈSE"};
                    episodes = linesExtractor.extractMultilines(episodes, "ÉPISODE", nextTagsEpisodes, lines, lineNumber);

                    // Genese
                    String[] nextTagsGenese = {"ANECDOTES"};
                    genese = linesExtractor.extractMultilines(genese, "GENÈSE", nextTagsGenese, lines, lineNumber);

                    // Anecdotes
                    String[] nextTagsAnecdotes = {"RÉCOMPENSES"};
                    anecdotes = linesExtractor.extractMultilines(anecdotes, "ANECDOTE", nextTagsAnecdotes, lines, lineNumber);

                    // Recompenses
                    String[] nextTagsRecompenses = {};
                    recompenses = linesExtractor.extractMultilines(recompenses, "RÉCOMPENSES", nextTagsRecompenses, lines, lineNumber);
                }

                if(!ignoreManualCheck || !needsManualCheck[0]) {
                    Set<CreatorModel> authorsSet;
                    Set<CreatorModel> composers;
                    Set<CategoryModel> styles;
                    Set<CategoryModel> kinds;
                    Set<DistributionEntry> distributionEntries;
                    Set<Season> seasonsSet;
                    Set<Anecdote> anecdotesSet;
                    SagaModel saga = new SagaModel();
                    LOGGER.info("Build model");

                    if(title != null) {
                        saga.setTitle(title);
                        LOGGER.debug("TITLE : {}", saga.getTitle());

                        saga = sagaService.findOrCreate(saga.getTitle());

                        if(authors != null) {
                            authorsSet = creatorParser.parse(authors);
                            LOGGER.debug("AUTHORS : {}", authorsSet);
                        }

                        if(music != null) {
                            composers = creatorParser.parse(music);
                            LOGGER.debug("MUSIC : {}", composers);
                        }

                        if(origin != null && !origin.startsWith("-")) {
                            saga.setOrigin(origin);
                            LOGGER.debug("ORIGIN : {}", saga.getOrigin());
                        }

                        if(kind != null) {
                            kinds = categoryParser.parse(kind);
                            LOGGER.debug("KINDS: {}", kinds);
                        }

                        if(style != null) {
                            styles = categoryParser.parse(style);
                            LOGGER.debug("STYLES : {}", styles);
                        }

                        if(status != null) {
                            saga.setStatus(statusParser.parse(status));
                            LOGGER.debug("STATUS : {}", saga.getStatus());
                        }

                        if(creation != null) {
                            saga.setStartDate(creationParser.parse(creation));
                            LOGGER.debug("CREATION : {}", saga.getStartDate());
                        }

                        if (duration != null) {
                            saga.setDuration(durationParser.parse(duration));
                            LOGGER.debug("DURATION : {}", saga.getDuration());
                        }

                        if(website != null) {
                            saga.setUrl(website);
                            LOGGER.debug("WEBSITE : {}", saga.getUrl());
                        }

                        if(distribution != null) {
                            distributionEntries = distributionParser.parse(distribution, saga.getId());
                            distributionEntries.forEach(distributionEntry -> LOGGER.debug("{} - {}", distributionEntry.getActor(), distributionEntry.getRoles()));
                        }

                        if(synopsis != null) {
                            saga.setSynopsis(textParser.parse(synopsis));
                            LOGGER.debug("SYNOPSIS : {}", saga.getSynopsis());
                        }

                        if(episodes != null) {
                            episodeParser.parse(episodes, saga.getId());
                        }

                        if(genese != null) {
                            saga.setGenese(textParser.parse(genese));
                            LOGGER.debug("GENESE : {}", saga.getGenese());
                        }

                        if(anecdotes != null) {
                            anecdotesSet = anecdoteParser.parse(anecdotes, saga.getId());
                            LOGGER.debug("ANECDOTES :");
                            anecdotesSet.forEach(anecdote -> LOGGER.debug("- {}", anecdote));
                        }

                        if(recompenses != null) {
                            saga.setAwards(recompenses);
                            LOGGER.debug("AWARDS : {}", saga.getAwards());
                        }

                        sagaService.update(saga);

                    }

                } else {
                    LOGGER.warn("Build model of {} ignored", content);
                }

            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }


}
