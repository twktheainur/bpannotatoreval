package org.pratikpharma.cli;


import org.json.simple.parser.ParseException;
import org.pratikpharma.ehealthtask.quaero.DirectQuaeroAnnotator;
import org.pratikpharma.ehealthtask.quaero.QaeroAnnotator;
import org.pratikpharma.io.quaero.QuaeroReader;
import org.pratikpharma.util.FiniteIterable;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotations.umls.UMLSSemanticGroupsLoader;
import org.sifrproject.annotatorclient.BioportalAnnotatorFactory;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class QuaeroEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(QuaeroEvaluator.class);
    static final String PRODUCTION_KEY = "907d47d9-3a00-4aa7-9111-090112dfab6a";
    static final String STAGE_KEY = "22522d5c-c4fe-45fc-afc6-d43e2e613169";
    static final double PERCENT_MAX = 100d;
    private static final Pattern EXTENTION_SEPARATOR = Pattern.compile("[.]");

    private QuaeroEvaluator() {
    }
    //private static Logger logger = LoggerFactory.getLogger(QuaeroEvaluator.class);

    @SuppressWarnings({"LawOfDemeter", "HardcodedLineSeparator"})
    public static void main(final String... args) throws IOException, NCBOAnnotatorErrorException, ParseException {


        @SuppressWarnings("HardcodedFileSeparator") final FiniteIterable<Map.Entry<String, String>> quaeroCorpus = new QuaeroReader(args[0]).load();

        final String format = args[2];
        final boolean expandMappings = Boolean.valueOf(args[3]);
        final Path resultDirectory = Paths.get(args[1]);

        //Loading type groups


        //final BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://services.bioportal.lirmm.fr/annotator/", PRODUCTION_KEY);
        final BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://localhost:8080/", PRODUCTION_KEY);

        final String[] ontologies = new String[args.length - 4];

        System.arraycopy(args, 4, ontologies, 0, args.length - 4);

        final String[] semanticGroups = {"CHEM", "DISO", "LIVB", "PROC", "ANAT", "PHYS", "OBJC", "GEOG", "DEVI", "PHEN"};
        final QaeroAnnotator qaeroAnnotator = new DirectQuaeroAnnotator(
                annotator,
                UMLSSemanticGroupsLoader.load(),
                ontologies,
                semanticGroups,
                format,
                expandMappings, resultDirectory);
        final List<String> alreadyCompleted = Files
                .list(resultDirectory)
                .map(path -> EXTENTION_SEPARATOR.split(path
                        .getFileName()
                        .toString())[0])
                .collect(Collectors.toList());
        final double size = quaeroCorpus.size() - alreadyCompleted.size();

        logger.info("Already processed: {}", String.join(" ",alreadyCompleted));

        if(size==0){
            logger.info("Nothing to do! If you want to run again, change the result directory or delete {}", resultDirectory);
        }
        double progress = 0d;
        for (final Map.Entry<String, String> textEntry : quaeroCorpus) {
            //noinspection HardcodedFileSeparator
            if (!alreadyCompleted.contains(textEntry.getKey())) {
                logger.info("\r[{}%] Annotating text '{}'", String.format("%2.2f", (progress / size) * PERCENT_MAX),textEntry.getKey());
                qaeroAnnotator.annotateText(textEntry.getValue(), textEntry.getKey());
                progress += 1;
            }
        }
        logger.info("\r[{}%] Done!", String.format("%2.2f", (progress / size) * PERCENT_MAX));
        //logger.info(processedText.toString());
    }
}
