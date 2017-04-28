package org.pratikpharma.experments;


import org.json.simple.parser.ParseException;
import org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator;
import org.pratikpharma.ehealthtask.task12017.MFCTypes;
import org.pratikpharma.io.ehealth2017.EHealth2017Task1Reader;
import org.pratikpharma.io.ehealth2017.corpus.Document;
import org.sifrproject.annotations.exceptions.InvalidFormatException;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotatorclient.BioportalAnnotatorFactory;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public final class CLEFEHealth2017Task1 {
    private CLEFEHealth2017Task1() {
    }
    private static final Logger logger = LoggerFactory.getLogger(CLEFEHealth2017Task1.class);

    @SuppressWarnings("LocalVariableOfConcreteClass")
    public static void main(final String... args) throws IOException, NCBOAnnotatorErrorException, ParseException, InvalidFormatException {


        logger.info("Loading corpus...");
        final Iterable<Document> corpus = new EHealth2017Task1Reader(args[0]).load();

        final PrintWriter printWriter = new PrintWriter(args[1]);

        final String cachePrefix = args[2];



        //Loading type groups


        final BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator(args[3], args[4]);

        final MFCTypes type = findType(args[5]);


        final String[] remainingArgs = Arrays.copyOfRange(args,6, args.length);

        try (Jedis jedis = new Jedis("localhost")) {
            final EHealth2017Task1Annotator eHealth2017Task1Annotator = new EHealth2017Task1Annotator(annotator, jedis,cachePrefix, type);

            logger.info("Starting annotation with BP proxy...");
            eHealth2017Task1Annotator.annotate(corpus,printWriter, remainingArgs);
        }

        //logger.info(processedText.toString());
    }

    private static MFCTypes findType(final String name){
        MFCTypes returnedType = MFCTypes.NONE;
        for(final MFCTypes type: MFCTypes.values()){
            if(name.equals(type.name())){
                returnedType = type;
            }
        }
        return returnedType;
    }
}
