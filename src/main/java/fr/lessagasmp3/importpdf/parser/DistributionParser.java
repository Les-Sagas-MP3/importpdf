package fr.lessagasmp3.importpdf.parser;

import fr.lessagasmp3.core.entity.DistributionEntry;
import fr.lessagasmp3.importpdf.service.CreatorService;
import fr.lessagasmp3.importpdf.service.DistributionEntryService;
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
    private CreatorService creatorService;

    @Autowired
    private DistributionEntryService distributionEntryService;

    public Set<DistributionEntry> parse(String distributionString, Long sagaId) {
        String[] lines = distributionString.split("\n");
        Set<DistributionEntry> distributionEntries = new LinkedHashSet<>();
        for(int lineNumber = 0 ; lineNumber < lines.length ; lineNumber++) {
            String[] lineSplit = lines[lineNumber].split(" - ");
            StringBuilder currentRoles;
            if(lineSplit.length == 2) {
                currentRoles = new StringBuilder(lineSplit[1]);
                if(lineNumber+1 < lines.length) {
                    boolean newCreatorFound = lines[lineNumber+1].split(" - ").length > 1;
                    while(!newCreatorFound) {
                        lineNumber++;
                        currentRoles.append(lines[lineNumber]);
                        if(lineNumber+1 < lines.length) {
                            newCreatorFound = lines[lineNumber+1].split(" - ").length > 1;
                        } else {
                            newCreatorFound = true;
                        }
                    }
                }
                distributionEntries.add(
                        DistributionEntry.fromModel(
                                distributionEntryService.findOrCreate(
                                        creatorService.findOrCreate(lineSplit[0]).getId(),
                                        sagaId,
                                        currentRoles.toString())));
            }
        }
        return distributionEntries;
    }


}
