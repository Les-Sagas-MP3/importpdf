package fr.lessagasmp3.importpdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

public class TerminateBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateBean.class);

    @PreDestroy
    public void onDestroy() {
        LOGGER.info("Application stopped");
    }
}