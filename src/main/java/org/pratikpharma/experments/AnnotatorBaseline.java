package org.pratikpharma.experments;


import org.json.simple.parser.ParseException;
import org.pratikpharma.ehealthtask.quaero.DirectQuaeroAnnotator;
import org.pratikpharma.ehealthtask.quaero.QaeroAnnotator;
import org.pratikpharma.io.QuaeroRawReader;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotations.umls.UMLSSemanticGroupsLoader;
import org.sifrproject.annotatorclient.BioportalAnnotatorFactory;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public final class AnnotatorBaseline {
    static final String PRODUCTION_KEY = "907d47d9-3a00-4aa7-9111-090112dfab6a";
    static final String STAGE_KEY = "22522d5c-c4fe-45fc-afc6-d43e2e613169";

    private AnnotatorBaseline() {
    }
    //private static Logger logger = LoggerFactory.getLogger(AnnotatorBaseline.class);

    public static void main(final String... args) throws IOException, NCBOAnnotatorErrorException, ParseException {


        @SuppressWarnings("HardcodedFileSeparator") final Iterable<Map.Entry<String, String>> quaeroCorpus = new QuaeroRawReader(args[0]).load();

        final String format = args[2];
        final boolean expandMappings=Boolean.valueOf(args[3]);

        //Loading type groups


        final BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://localhost:8082/annotator/", PRODUCTION_KEY);

        final String[] ontologies = new String[args.length - 4];

        System.arraycopy(args, 4, ontologies, 0, args.length - 4);

        final String[] semanticGroups = {"ANAT", "CHEM", "DEVI", "DISO", "GEOG", "LIVB", "OBJC", "PHEN", "PHYS", "PROC"};
        final QaeroAnnotator qaeroAnnotator = new DirectQuaeroAnnotator(
                annotator,
                UMLSSemanticGroupsLoader.load(),
                ontologies,
                semanticGroups,
                format,
                expandMappings);

        for (final Map.Entry<String, String> textEntry : quaeroCorpus) {
            //noinspection HardcodedFileSeparator
            qaeroAnnotator.annotateText(textEntry.getValue(), textEntry.getKey(), Paths.get(args[1]));
        }
        //logger.info(processedText.toString());
    }
}
