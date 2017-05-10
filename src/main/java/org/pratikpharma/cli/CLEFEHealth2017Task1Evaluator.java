package org.pratikpharma.cli;


import org.json.simple.parser.ParseException;
import org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator;
import org.pratikpharma.ehealthtask.task12017.PostTypes;
import org.pratikpharma.io.ehealth2017.corpus.Document;
import org.pratikpharma.io.ehealth2017.corpus.reader.EHealth2017Task1AlignedReader;
import org.pratikpharma.io.ehealth2017.corpus.reader.EHealth2017Task1RawReader;
import org.sifrproject.annotations.exceptions.InvalidFormatException;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotatorclient.BioportalAnnotatorFactory;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparqy.graph.storage.JenaRemoteSPARQLStore;
import org.sparqy.graph.storage.StoreHandler;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;

public final class CLEFEHealth2017Task1Evaluator {
    private CLEFEHealth2017Task1Evaluator() {
    }
    private static final Logger logger = LoggerFactory.getLogger(CLEFEHealth2017Task1Evaluator.class);

    @SuppressWarnings("LocalVariableOfConcreteClass")
    public static void main(final String... args) throws IOException, NCBOAnnotatorErrorException, ParseException, InvalidFormatException {


        logger.info("Loading corpus...");

        final String evalType = args[0];

        final Iterable<Document> corpus = evalType.toLowerCase().equals("raw") ?
                new EHealth2017Task1RawReader(args[1]).load() :
                new EHealth2017Task1AlignedReader(args[1]).load();


        final PrintWriter printWriter = new PrintWriter(args[2]);

        final String cachePrefix = args[3];

        final BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator(args[4], args[5]);
        final URL bioportalURL = new URL(args[4]);
        @SuppressWarnings("HardcodedFileSeparator") final String fourStoreURL = "http://"+ bioportalURL.getHost()+":9000/sparql/";

        StoreHandler.registerStoreInstance(new JenaRemoteSPARQLStore(fourStoreURL));

        final PostTypes type = findType(args[6]);

        final double cutoffThreshold = Double.valueOf(args[7]);


        final String[] remainingArgs = Arrays.copyOfRange(args,8, args.length);

        try (JedisPool jedisPool = new JedisPool("localhost")) {
            final EHealth2017Task1Annotator eHealth2017Task1Annotator = new EHealth2017Task1Annotator(annotator, jedisPool,cachePrefix, type, cutoffThreshold, evalType.toLowerCase().equals("raw"));

            logger.info("Starting annotation with BP proxy...");
            eHealth2017Task1Annotator.annotate(corpus,printWriter, remainingArgs);
        }

        //logger.info(processedText.toString());
    }

    private static PostTypes findType(final String name){
        PostTypes returnedType = PostTypes.NONE;
        for(final PostTypes type: PostTypes.values()){
            if(name.equals(type.name())){
                returnedType = type;
            }
        }
        return returnedType;
    }
}
