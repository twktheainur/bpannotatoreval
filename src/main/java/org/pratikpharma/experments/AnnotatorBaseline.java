package org.pratikpharma.experments;


import org.json.simple.parser.ParseException;
import org.pratikpharma.ehealthtask.DirectQuaeroAnnotator;
import org.pratikpharma.ehealthtask.TaskAnnotator;
import org.pratikpharma.io.QuaeroRawReader;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotations.umls.UMLSSemanticGroupsLoader;
import org.sifrproject.annotatorclient.BioportalAnnotatorFactory;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public final class AnnotatorBaseline {
    private AnnotatorBaseline() {
    }
    //private static Logger logger = LoggerFactory.getLogger(AnnotatorBaseline.class);

    public static void main(final String... args) throws IOException, NCBOAnnotatorErrorException, ParseException {


        @SuppressWarnings("HardcodedFileSeparator") final Iterable<Map.Entry<String, String>> quaeroCorpus = new QuaeroRawReader(args[0]).load();

        //Loading type groups


        final BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://localhost:8082", "907d47d9-3a00-4aa7-9111-090112dfab6a");

        final String[] ontologies = {"MSHFRE", "MDRFRE", "MTHMSTFRE", "MEDLINEPLUS", "CIM-10"};
        final String[] semanticGroups = {};
        final TaskAnnotator taskAnnotator = new DirectQuaeroAnnotator(
                annotator,
                UMLSSemanticGroupsLoader.load(),
                ontologies,
                semanticGroups,
                "quaerosg",
                true);

        for (final Map.Entry<String, String> textEntry : quaeroCorpus) {
            //noinspection HardcodedFileSeparator
            taskAnnotator.annotateText(textEntry.getValue(), textEntry.getKey(), Paths.get(args[1]));
        }
        //logger.info(processedText.toString());
    }
}
