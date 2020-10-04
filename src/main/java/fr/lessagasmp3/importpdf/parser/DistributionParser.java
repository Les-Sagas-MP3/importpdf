package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.core.entity.Creator;
import fr.lessagasmp3.core.entity.DistributionEntry;
import fr.lessagasmp3.importpdf.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class DistributionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionParser.class);

    @Autowired
    private AuthorService authorService;

    public Set<DistributionEntry> parse(String distributionString) {
        String[] lines = distributionString.split("\n");
        Set<DistributionEntry> distributionEntries = new LinkedHashSet<>();
        for(int lineNumber = 0 ; lineNumber < lines.length ; lineNumber++) {
            String[] lineSplit = lines[lineNumber].split(" - ");
            LOGGER.info(lines[lineNumber]);
            StringBuilder currentRoles;
            if(lineSplit.length == 2) {
                DistributionEntry distributionEntry = new DistributionEntry();
                distributionEntry.setActor(Creator.fromModel(authorService.findOrCreate(lineSplit[0])));
                currentRoles = new StringBuilder(lineSplit[1]);
                if(lineNumber+1 < lines.length) {
                    boolean newCreatorFound = lines[lineNumber+1].split(" - ").length > 1;
                    while(!newCreatorFound) {
                        lineNumber++;
                        LOGGER.info(lines[lineNumber]);
                        currentRoles.append(lines[lineNumber]);
                        if(lineNumber+1 < lines.length) {
                            newCreatorFound = lines[lineNumber+1].split(" - ").length > 1;
                        } else {
                            newCreatorFound = true;
                        }
                    }
                }
                distributionEntry.setRoles(currentRoles.toString());
                distributionEntries.add(distributionEntry);
            }
        }
        return distributionEntries;
    }


}
