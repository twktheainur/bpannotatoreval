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

        final String format = args[2];
        final boolean expandMappings=Boolean.valueOf(args[3]);

        //Loading type groups


        final BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://localhost:8080/annotator", "907d47d9-3a00-4aa7-9111-090112dfab6a");

        String[] ontologies = new String[args.length - 4];

        for(int i=4;i< args.length;i++){
            ontologies[i-4] = args[i];
        }

        final String[] semanticGroups = {"ANAT", "CHEM", "DEVI", "DISO", "GEOG", "LIVB", "OBJC", "PHEN", "PHYS", "PROC"};
        final TaskAnnotator taskAnnotator = new DirectQuaeroAnnotator(
                annotator,
                UMLSSemanticGroupsLoader.load(),
                ontologies,
                semanticGroups,
                format,
                expandMappings);

        for (final Map.Entry<String, String> textEntry : quaeroCorpus) {
            //noinspection HardcodedFileSeparator
            taskAnnotator.annotateText(textEntry.getValue(), textEntry.getKey(), Paths.get(args[1]));
        }
        //logger.info(processedText.toString());
    }
}
