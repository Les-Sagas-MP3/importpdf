package fr.lessagasmp3.importpdf;

import fr.lessagasmp3.core.entity.Anecdote;
import fr.lessagasmp3.core.entity.DistributionEntry;
import fr.lessagasmp3.core.model.CategoryModel;
import fr.lessagasmp3.core.model.CreatorModel;
import fr.lessagasmp3.core.model.SagaModel;
import fr.lessagasmp3.importpdf.extractor.*;
import fr.lessagasmp3.importpdf.parser.*;
import fr.lessagasmp3.importpdf.service.SagaService;
import org.apache.commons.io.FilenameUtils;
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
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ImportpdfApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportpdfApplication.class);
    private static final String CREATION = LinesExtractor.convertToUtf8("CRÉATION");
    private static final String SERIE = LinesExtractor.convertToUtf8("SÉRIE");
    private static final String EPISODE = LinesExtractor.convertToUtf8("ÉPISODE");
    private static final String GENESE = LinesExtractor.convertToUtf8("GENÈSE");
    private static final String RECOMPENSE = LinesExtractor.convertToUtf8("RÉCOMPENSE");

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

    private long lastUploadTs = 0L;

    static {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
    }

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

        String inputFolderPath = rootFolderPath + File.separator + "input";
        File inputFolder = new File(inputFolderPath);
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            LOGGER.error("The path {} does not exist or is not a directory", inputFolderPath);
            throw new IllegalArgumentException();
        }
        LOGGER.info("{} : OK", inputFolderPath);

        String pdfsFolderPath = inputFolderPath + File.separator + "pdfs";
        File pdfsFolder = new File(pdfsFolderPath);
        if (!pdfsFolder.exists() || !pdfsFolder.isDirectory()) {
            LOGGER.error("The path {} does not exist or is not a directory", pdfsFolderPath);
            throw new IllegalArgumentException();
        }
        LOGGER.info("{} : OK", pdfsFolderPath);

        String imagesFolderPath = inputFolderPath + File.separator + "images";
        File imagesFolder = new File(pdfsFolderPath);
        if (!imagesFolder.exists() || !imagesFolder.isDirectory()) {
            LOGGER.error("The path {} does not exist or is not a directory", imagesFolderPath);
            throw new IllegalArgumentException();
        }
        LOGGER.info("{} : OK", imagesFolderPath);

        String outputFolderPath = rootFolderPath + File.separator + "output";
        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists() || !outputFolder.isDirectory()) {
            LOGGER.error("The path {} does not exist or is not a directory", outputFolderPath);
            throw new IllegalArgumentException();
        }
        LOGGER.info("{} : OK", outputFolderPath);

        String[] contents = pdfsFolder.list();
        if (contents == null) {
            LOGGER.error("No pdf was detected");
            throw new IllegalArgumentException();
        }
/*
        for (int i = 0; i < 1; i++) {
            String content = "Donjon de Naheulbeuk.pdf";
            //String content = "Dieu en peignoir (le).pdf";
            //String content = "Crash  La revanche.pdf";
            //String content = "Ⅲème Légion.pdf";
            //String content = "#Pauvirés.pdf";
            String title = parseFile(pdfsFolderPath, content);
            moveFile(pdfsFolderPath, content, cleanFilename(title), "data.pdf");
        }
*/
        for (String content : contents) {
            String title = parseFile(pdfsFolderPath, content);
            moveFile(pdfsFolderPath, content, cleanFilename(title), "data.pdf");
        }

        ctx.close();

    }

    private String parseFile(String folderPath, String content) {

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
        SagaModel saga = new SagaModel();

        String pdfPath = folderPath + File.separator + content;
        LOGGER.info("File : {}", content);

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {

            content = LinesExtractor.convertToUtf8(content);

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
                String[] rawLines = pdfFileInText.split("\\r?\\n");
                String[] lines = new String[rawLines.length];
                for (int lineNumber = 0; lineNumber < rawLines.length; lineNumber++) {
                    lines[lineNumber] = LinesExtractor.convertToUtf8(rawLines[lineNumber]);
                }
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
                        String[] nextTagStyle = {CREATION, "STATUT"};
                        style = linesExtractor.extractLines(style, "STYLE", nextTagStyle, lines, lineNumber);

                        // Status
                        String[] nextTagStatus = {CREATION, "SAISON", SERIE, "ARC", "1ER " + SERIE, "BLOC 1", "CYCLE 1", "OPUS 1"};
                        status = linesExtractor.extractLines(status, "STATUT", nextTagStatus, lines, lineNumber);

                        // Creation
                        String[] nextTagCreation = {"STATUT", "SAISON", SERIE, "ARC", "1ER " + SERIE, "BLOC 1", "CYCLE 1", "OPUS 1"};
                        creation = linesExtractor.extractLines(creation, CREATION, nextTagCreation, lines, lineNumber);

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
                    String[] nextTagsSynopsis = {EPISODE, "ANECDOTE", GENESE};
                    synopsis = linesExtractor.extractMultilines(synopsis, "SYNOPSIS", nextTagsSynopsis, lines, lineNumber);

                    // Episodes
                    String[] nextTagsEpisodes = {"ANECDOTE", GENESE};
                    episodes = linesExtractor.extractMultilines(episodes, EPISODE, nextTagsEpisodes, lines, lineNumber);

                    // Genese
                    String[] nextTagsGenese = {"ANECDOTE"};
                    genese = linesExtractor.extractMultilines(genese, GENESE, nextTagsGenese, lines, lineNumber);

                    // Anecdotes
                    String[] nextTagsAnecdotes = {RECOMPENSE};
                    anecdotes = linesExtractor.extractMultilines(anecdotes, "ANECDOTE", nextTagsAnecdotes, lines, lineNumber);

                    // Recompenses
                    String[] nextTagsRecompenses = {};
                    recompenses = linesExtractor.extractMultilines(recompenses, RECOMPENSE, nextTagsRecompenses, lines, lineNumber);
                }

                if (!ignoreManualCheck || !needsManualCheck[0]) {
                    Set<CreatorModel> authorsSet;
                    Set<CreatorModel> composers;
                    Set<CategoryModel> styles;
                    Set<CategoryModel> kinds;
                    Set<DistributionEntry> distributionEntries;
                    Set<Anecdote> anecdotesSet;
                    LOGGER.info("Build model");

                    if (title != null && !title.isEmpty() && !LinesExtractor.removeLastSpaces(title).isEmpty()) {
                        saga.setTitle(LinesExtractor.removeLastSpaces(title));
                        LOGGER.debug("TITLE : {}", saga.getTitle());

                        saga = sagaService.findOrCreate(saga.getTitle());

                        if (authors != null) {
                            authorsSet = creatorParser.parse(authors);
                            LOGGER.debug("AUTHORS : {}", authorsSet);
                            SagaModel finalSaga = saga;
                            authorsSet.forEach(author -> sagaService.addAuthor(finalSaga.getId(), author.getId()));
                        }

                        if (music != null) {
                            composers = creatorParser.parse(music);
                            LOGGER.debug("MUSIC : {}", composers);
                            SagaModel finalSaga = saga;
                            composers.forEach(composer -> sagaService.addComposer(finalSaga.getId(), composer.getId()));
                        }

                        if (origin != null && !origin.startsWith("-")) {
                            saga.setOrigin(LinesExtractor.removeLastSpaces(origin));
                            LOGGER.debug("ORIGIN : {}", saga.getOrigin());
                        }

                        if (kind != null) {
                            kinds = categoryParser.parse(kind);
                            LOGGER.debug("KINDS: {}", kinds);
                            SagaModel finalSaga = saga;
                            kinds.forEach(oneKind -> sagaService.addCategory(finalSaga.getId(), oneKind.getId()));
                        }

                        if (style != null) {
                            styles = categoryParser.parse(style);
                            LOGGER.debug("STYLES : {}", styles);
                            SagaModel finalSaga = saga;
                            styles.forEach(oneStyle -> sagaService.addCategory(finalSaga.getId(), oneStyle.getId()));
                        }

                        if (status != null) {
                            saga.setStatus(statusParser.parse(status));
                            LOGGER.debug("STATUS : {}", saga.getStatus());
                        }

                        if (creation != null) {
                            saga.setStartDate(creationParser.parse(creation));
                            LOGGER.debug("CREATION : {}", saga.getStartDate());
                        }

                        if (duration != null) {
                            saga.setDuration(durationParser.parse(duration));
                            LOGGER.debug("DURATION : {}", saga.getDuration());
                        }

                        if (website != null) {
                            saga.setUrl(LinesExtractor.removeLastSpaces(website));
                            LOGGER.debug("WEBSITE : {}", saga.getUrl());
                        }

                        if (distribution != null) {
                            distributionEntries = distributionParser.parse(distribution, saga.getId());
                            distributionEntries.forEach(distributionEntry -> LOGGER.debug("{} - {}", distributionEntry.getActor(), distributionEntry.getRoles()));
                        }

                        if (synopsis != null) {
                            saga.setSynopsis(textParser.parse(synopsis));
                            LOGGER.debug("SYNOPSIS : {}", saga.getSynopsis());
                        }

                        if (episodes != null) {
                            episodeParser.parse(episodes, saga.getId());
                        }

                        if (genese != null) {
                            saga.setGenese(textParser.parse(genese));
                            LOGGER.debug("GENESE : {}", saga.getGenese());
                        }

                        if (anecdotes != null) {
                            anecdotesSet = anecdoteParser.parse(anecdotes, saga.getId());
                            LOGGER.debug("ANECDOTES :");
                            anecdotesSet.forEach(anecdote -> LOGGER.debug("- {}", anecdote));
                        }

                        if (recompenses != null) {
                            saga.setAwards(recompenses);
                            LOGGER.debug("AWARDS : {}", saga.getAwards());
                        }

                        sagaService.update(saga);

                        String path = rootFolderPath + File.separator + "output" + File.separator + cleanFilename(saga.getTitle());
                        LOGGER.debug("Create {} output path", path);
                        File theDir = new File(path);
                        if (!theDir.exists()) {
                            if(!theDir.mkdirs()) {
                                LOGGER.error("Cannot create {} dir", theDir.getPath());
                            }
                        }

                        upload(saga, "pochette", "cover");
                        upload(saga, "ban", "banner");
                    }

                } else {
                    LOGGER.warn("Build model of {} ignored", content);
                }

            }
        } catch (IOException | NumberFormatException | InterruptedException | IllegalStateException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return saga.getTitle();
    }

    private void upload(SagaModel saga, String imageType, String endpoint) throws InterruptedException {
        String imageFolderPath = rootFolderPath + File.separator + "input" + File.separator + "images";
        File f = new File(imageFolderPath);
        File[] matchingFiles = f.listFiles((dir, name) ->
                name.toLowerCase().contains(saga.getTitle().toLowerCase()) && name.toLowerCase().contains("- " + imageType));
        if (matchingFiles == null || matchingFiles.length == 0) {
            return;
        }
        Arrays.stream(matchingFiles).forEach(img -> LOGGER.debug(img.getName()));
        long currentTs = new Date().getTime();
        while(currentTs - lastUploadTs < 2000) {
            currentTs = new Date().getTime();
            Thread.sleep(1000);
        }
        sagaService.uploadImg(saga.getId(), matchingFiles[0], endpoint);
        lastUploadTs = new Date().getTime();
        moveFile(imageFolderPath, matchingFiles[0].getName(), cleanFilename(saga.getTitle()), imageType + "." + FilenameUtils.getExtension(matchingFiles[0].getName()));
    }

    private void moveFile(String folderPath, String content, String sagaTitle, String destName) {
        Path result = null;
        try {
            result = Files.move(Paths.get(folderPath + File.separator + content), Paths.get(rootFolderPath + File.separator + "output" + File.separator + sagaTitle + File.separator + destName));
        } catch (IOException | InvalidPathException e) {
            LOGGER.error("Exception while moving file: {}", e.getMessage(), e);
        }
        if (result != null) {
            System.out.println("File moved successfully.");
            LOGGER.info("File {} moved successfully", content);
        } else {
            LOGGER.error("File {} movement failed", content);
        }
    }

    private String cleanFilename(String str) {
        return LinesExtractor.removeLastSpaces(str.replace("?", "")
                .replace(":", "-"));
    }

}
