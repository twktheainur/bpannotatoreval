package org.pratikpharma.experments;


import org.json.simple.parser.ParseException;
import org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator;
import org.pratikpharma.io.ehealt2017.EHealth2017Task1Reader;
import org.pratikpharma.io.ehealt2017.corpus.Document;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotatorclient.BioportalAnnotatorFactory;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.PrintWriter;

public final class CLEFEHealth2017Task1 {
    private CLEFEHealth2017Task1() {
    }
    private static final Logger logger = LoggerFactory.getLogger(CLEFEHealth2017Task1.class);

    @SuppressWarnings("LocalVariableOfConcreteClass")
    public static void main(final String... args) throws IOException, NCBOAnnotatorErrorException, ParseException {


        logger.info("Loading corpus...");
        final Iterable<Document> corpus = new EHealth2017Task1Reader(args[0]).load();


        //Loading type groups


        final BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://localhost:8082/annotator/", AnnotatorBaseline.STAGE_KEY);

        final PrintWriter printWriter = new PrintWriter(args[1]);

        try (Jedis jedis = new Jedis("localhost")) {
            final EHealth2017Task1Annotator eHealth2017Task1Annotator = new EHealth2017Task1Annotator(annotator, jedis);

            logger.info("Starting annotation with BP proxy...");
            eHealth2017Task1Annotator.annotate(corpus,printWriter);
        }

        //logger.info(processedText.toString());
    }
}
