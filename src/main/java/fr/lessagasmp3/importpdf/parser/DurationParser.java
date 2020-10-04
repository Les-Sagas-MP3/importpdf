package fr.lessagasmp3.importpdf.parser;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class DurationParser {

    public Duration parse(String duration) {
        duration = duration.toLowerCase();
        if (duration.startsWith("-")) {
            return null;
        }
        duration = duration.replace("sec", "")
                .replace("s", "")
                .replace("(+)", "")
                .replace(">", "")
                .replace(" ", "");
        if (duration.contains("h")) {
            if (duration.endsWith("h")) {
                duration += "00:00";
            } else if (duration.endsWith("m") || duration.endsWith("mn") || duration.endsWith("min") || duration.endsWith("minute")) {
                duration += "00";
            } else if (!duration.contains("m")) {
                duration += ":00";
            }
        } else if (duration.contains("m")) {
            duration = "00:" + duration;
            if (duration.endsWith("m") || duration.endsWith("mn") || duration.endsWith("min") || duration.endsWith("minute")) {
                duration += "00";
            }
        } else {
            duration = "00:00:" + duration;
        }
        duration = duration.replace("h", ":")
                .replace("minute", ":")
                .replace("min", ":")
                .replace("mn", ":")
                .replace("m", ":");
        String[] splitDuration = duration.split(":");
        int hours = Integer.parseInt(splitDuration[0]);
        int minutes = Integer.parseInt(splitDuration[1]);
        int seconds = Integer.parseInt(splitDuration[2]);
        int days = 0;
        minutes+= seconds / 60;
        seconds = seconds % 60;
        hours+= minutes / 60;
        minutes = minutes % 60;
        days+= hours / 24;
        hours = hours % 24;
        duration = String.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds);
        return Duration.between(LocalTime.MIN, LocalTime.parse(duration, DateTimeFormatter.ofPattern("d:HH:mm:ss")));
    }

}
