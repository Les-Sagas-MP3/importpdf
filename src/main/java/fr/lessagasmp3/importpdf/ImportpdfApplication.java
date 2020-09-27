package fr.lessagasmp3.importpdf;

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

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, })
public class ImportpdfApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImportpdfApplication.class);

	@Autowired
	private ConfigurableApplicationContext ctx;

	@Value("${fr.lessagasmp3.importpdf.root.folder}")
	private String rootFolderPath;

	public static void main(String[] args) {
		SpringApplication.run(ImportpdfApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		LOGGER.info("Starting import");

		File rootFolder = new File(rootFolderPath);
		if(!rootFolder.exists() || !rootFolder.isDirectory()) {
			LOGGER.error("The path {} does not exist or is not a directory", rootFolderPath);
			throw new IllegalArgumentException();
		}
		LOGGER.info("{} : OK", rootFolderPath);

		String pdfsFolderPath = rootFolderPath + File.separator + "pdfs";
		File pdfsFolder = new File(pdfsFolderPath);
		if(!pdfsFolder.exists() || !pdfsFolder.isDirectory()) {
			LOGGER.error("The path {} does not exist or is not a directory", pdfsFolderPath);
			throw new IllegalArgumentException();
		}
		LOGGER.info("{} : OK", pdfsFolderPath);

		String imagesFolderPath = rootFolderPath + File.separator + "images";
		File imagesFolder = new File(pdfsFolderPath);
		if(!imagesFolder.exists() || !imagesFolder.isDirectory()) {
			LOGGER.error("The path {} does not exist or is not a directory", imagesFolderPath);
			throw new IllegalArgumentException();
		}
		LOGGER.info("{} : OK", imagesFolderPath);

		String[] contents = pdfsFolder.list();
		if(contents == null) {
			LOGGER.error("No pdf was detected");
			throw new IllegalArgumentException();
		}
/*
		for(int i = 0 ; i < 1 ; i++) {
			//String content = "Donjon de Naheulbeuk.pdf";
			//String content = "Dieu en peignoir (le).pdf";
			String content = "Crash  La revanche.pdf";
			String pdfPath = pdfsFolderPath + File.separator + content;
			LOGGER.info("Opening {}", content);
			parseFile(pdfPath);
		}
*/
		for (String content : contents) {
			String pdfPath = pdfsFolderPath + File.separator + content;
			LOGGER.info("File : {}", content);
			parseFile(pdfPath);
		}

		ctx.close();

	}

	private void parseFile(String pdfPath) {

		// Output data
		String authors = null;
		String music = null;
		String origin = null;
		String kind = null;
		String style= null;
		String status = null;
		String creation = null;
		String duration = null;
		String bonus = null;
		String website = null;
		String distribution = null;
		String title = null;
		String synopsis = null;
		String episodes = null;
		String anecdotes = null;
		String genese = null;

		try (PDDocument document = PDDocument.load(new File(pdfPath))) {
			if (!document.isEncrypted()) {
				PDFTextStripperByArea stripper = new PDFTextStripperByArea();
				stripper.setSortByPosition(true);
				PDFTextStripper tStripper = new PDFTextStripper();
				String pdfFileInText = tStripper.getText(document);

				//split by whitespace
				String[] lines = pdfFileInText.split("\\r?\\n");
				for (int lineNumber = 0 ; lineNumber < lines.length ; lineNumber++) {
					String line = lines[lineNumber];
					LOGGER.debug("Analyzing line {} : {}", lineNumber, line);

					// Table on left
					String[] lineSplitColon = line.split(": ");
					if(lineSplitColon.length >= 2) {

						// Author
						String[] nextTagsAuthor = {"MUSIQUE", "ORIGIN"};
						authors = parseLines(authors, "AUTEUR", nextTagsAuthor, lines, lineNumber);

						// Music
						String[] nextTagsMusic = {"ORIGIN"};
						music = parseLines(music, "MUSIQUE", nextTagsMusic, lines, lineNumber);

						// Origin
						String[] nextTagOrigin = {"GENRE"};
						origin = parseLines(origin, "ORIGIN", nextTagOrigin, lines, lineNumber);

						// Kind
						String[] nextTagKind = {"STYLE"};
						kind = parseLines(kind, "GENRE", nextTagKind, lines, lineNumber);

						// Style
						String[] nextTagStyle = {"CRÉATION", "STATUT"};
						style = parseLines(style, "STYLE", nextTagStyle, lines, lineNumber);

						// Status
						String[] nextTagStatus = {"CRÉATION", "SAISON", "SÉRIE", "ARC", "1ER SÉRIE", "BLOC 1", "CYCLE 1", "OPUS 1"};
						status = parseLines(status, "STATUT", nextTagStatus, lines, lineNumber);

						// Creation
						String[] nextTagCreation = {"STATUT", "SAISON", "SÉRIE", "ARC", "1ER SÉRIE", "BLOC 1", "CYCLE 1", "OPUS 1"};
						creation = parseLines(creation, "CRÉATION", nextTagCreation, lines, lineNumber);

						// Duration
						String[] nextTagDuration = {"BONUS"};
						duration = parseLines(duration, "DURÉE", nextTagDuration, lines, lineNumber);

						// Bonus
						bonus = parseBonus(bonus, lines, lineNumber);
					}

					// Website
					website = parseWebsite(website, lines, lineNumber);

					// Distribution
					if(bonus != null && title == null && !line.contains("Bonus : ") && (line.split(" - ").length >= 2 || line.split(" : ").length >= 2)) {
						distribution = parseDistribution(distribution, lines, lineNumber);
					}

					// Title
					title = parseTitle(title, lines, lineNumber);

					// Synopsis
					String[] nextTagsSynopsis = {"ÉPISODE", "ANECDOTE", "GENÈSE"};
					synopsis = parseMultilines(synopsis, "SYNOPSIS", nextTagsSynopsis, lines, lineNumber);

					// Episodes
					String[] nextTagsEpisodes = {"ANECDOTE", "GENÈSE"};
					episodes = parseMultilines(episodes, "ÉPISODE", nextTagsEpisodes, lines, lineNumber);

					// Anecdotes
					String[] nextTagsAnecdotes = {"GENÈSE"};
					anecdotes = parseMultilines(anecdotes, "ANECDOTE", nextTagsAnecdotes, lines, lineNumber);

					// Anecdotes
					String[] nextTagsGenese = {};
					genese = parseMultilines(genese, "GENÈSE", nextTagsGenese, lines, lineNumber);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String parseLines(String lineParsed, String enterringTag, String[] nextTags, String[] lines, int lineNumber) {
		String line = lines[lineNumber];
		if(lineParsed == null && line.toUpperCase().startsWith(enterringTag)) {
			LOGGER.debug("{} recognized", enterringTag);
			StringBuilder separatedEntities = new StringBuilder();
			String[] lineSplitColon = line.split(": ");
			separatedEntities.append(lineSplitColon[1]);
			String nextLine = lines[lineNumber+1].toUpperCase();
			boolean reachedNextTag = false;
			for(String nextTag : nextTags) {
				reachedNextTag|= nextLine.startsWith(nextTag);
			}
			while(!reachedNextTag) {
				lineNumber++;
				separatedEntities.append(lines[lineNumber]);
				LOGGER.debug(lines[lineNumber]);
				nextLine = lines[lineNumber+1].toUpperCase();
				for(String nextTag : nextTags) {
					reachedNextTag|= nextLine.startsWith(nextTag);
				}
			}
			lineParsed = separatedEntities.toString();
			LOGGER.debug("{} : {}", enterringTag, lineParsed);
		}
		return lineParsed;
	}

	private String parseBonus(String lineParsed, String[] lines, int lineNumber) {
		String enterringTag = "BONUS";
		String line = lines[lineNumber];
		if(lineParsed == null && line.toUpperCase().startsWith(enterringTag)) {
			LOGGER.debug("{} recognized", enterringTag);
			StringBuilder separatedEntities = new StringBuilder();
			String[] lineSplitColon = line.split(": ");
			separatedEntities.append(lineSplitColon[1]);
			lineParsed = separatedEntities.toString();
			LOGGER.debug("{} : {}", enterringTag, lineParsed);
		}
		return lineParsed;
	}

	private String parseWebsite(String lineParsed, String[] lines, int lineNumber) {
		String line = lines[lineNumber];
		if(lineParsed == null && (line.startsWith("http://") || line.startsWith("https://"))) {
			LOGGER.debug("WEBSITE recognized");
			StringBuilder separatedEntities = new StringBuilder();
			separatedEntities.append(line);
			String nextLine = lines[lineNumber+1].toUpperCase();
			boolean reachedNextPart = nextLine.indexOf(" ") != nextLine.length()-1 && nextLine.contains(" ");
			while(!reachedNextPart) {
				lineNumber++;
				separatedEntities.append(lines[lineNumber]);
				LOGGER.debug(lines[lineNumber]);
				nextLine = lines[lineNumber+1].toUpperCase();
				reachedNextPart = nextLine.contains(" ");
			}
			lineParsed = separatedEntities.toString();
			LOGGER.debug("WEBSITE : {}", lineParsed);
		}
		return lineParsed;
	}

	private String parseDistribution(String lineParsed, String[] lines, int lineNumber) {
		String line = lines[lineNumber];
		if(lineParsed == null && !(line.startsWith("http://") || line.startsWith("https://")) && !lines[lineNumber+1].toUpperCase().contains("SYNOPSIS")) {
			LOGGER.debug("DISTRIBUTION recognized");
			StringBuilder separatedEntities = new StringBuilder();
			separatedEntities.append(line);
			separatedEntities.append("\n");
			String nextLine = lines[lineNumber+2].toUpperCase();
			boolean reachedNextTag = nextLine.contains("SYNOPSIS");
			while(!reachedNextTag) {
				lineNumber++;
				line = lines[lineNumber];
				separatedEntities.append(line);
				separatedEntities.append("\n");
				LOGGER.debug(lines[lineNumber]);
				nextLine = lines[lineNumber+2].toUpperCase();
				reachedNextTag = nextLine.contains("SYNOPSIS");
			}
			lineParsed = separatedEntities.toString();
			LOGGER.debug("DISTRIBUTION : {}", lineParsed);
		}
		return lineParsed;
	}

	private String parseTitle(String lineParsed, String[] lines, int lineNumber) {
		if(lineParsed == null && lines[lineNumber+1].toUpperCase().contains("SYNOPSIS")) {
			LOGGER.debug("TITLE recognized");
			lineParsed = lines[lineNumber];
			LOGGER.debug("TITLE : {}", lineParsed);
		}
		return lineParsed;
	}

	private String parseMultilines(String lineParsed, String enterringTag, String[] nextTags, String[] lines, int lineNumber) {
		if(lineParsed == null && lines[lineNumber].toUpperCase().startsWith(enterringTag)) {
			LOGGER.debug("{} recognized", enterringTag);
			StringBuilder separatedEntities = new StringBuilder();
			String nextLine = lines[lineNumber+1].toUpperCase();
			boolean reachedNextTag = lineNumber+1 >= lines.length;
			for(String nextTag : nextTags) {
				reachedNextTag|= nextLine.startsWith(nextTag);
			}
			while(!reachedNextTag) {
				lineNumber++;
				separatedEntities.append(lines[lineNumber]);
				separatedEntities.append("\n");
				LOGGER.debug(lines[lineNumber]);
				if(lineNumber+1 < lines.length) {
					nextLine = lines[lineNumber + 1].toUpperCase();
					for(String nextTag : nextTags) {
						reachedNextTag|= nextLine.startsWith(nextTag);
					}
				} else {
					reachedNextTag = true;
				}
			}
			lineParsed = separatedEntities.toString();
			LOGGER.debug("{} : {}", enterringTag, lineParsed);
		}
		return lineParsed;
	}



}
