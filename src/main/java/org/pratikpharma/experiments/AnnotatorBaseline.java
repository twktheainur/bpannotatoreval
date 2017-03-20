package org.pratikpharma.experiments;


import org.getalp.lexsema.util.Language;
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
    //parameters.add(new PairImpl<>("apikey","1de0a270-29c5-4dda-b043-7c3580628cd5"));
    //parameters.add(new PairImpl<>("apikey","33cf147d-f54a-4e70-ac32-139af89b609e"));
    //http://data.bioontology.org/annotator
    //private static Logger logger = LoggerFactory.getLogger(AnnotatorBaseline.class);

    public static void main(final String... args) throws IOException, NCBOAnnotatorErrorException, ParseException {


        @SuppressWarnings("HardcodedFileSeparator") final Iterable<Map.Entry<String,String>> quaeroCorpus = new QuaeroRawReader(args[0]).load();

        //Loading type groups


        final BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://services.stageportal.lirmm.fr/annotator/", "22522d5c-c4fe-45fc-afc6-d43e2e613169");

        for( final Map.Entry<String,String> textEntry : quaeroCorpus) {
            final TaskAnnotator taskAnnotator = new DirectQuaeroAnnotator(UMLSSemanticGroupsLoader.load(), annotator);
            //noinspection HardcodedFileSeparator
            taskAnnotator.annotateText(textEntry.getValue(), textEntry.getKey(), Language.FRENCH, Paths.get(args[1]));
        }
        //logger.info(processedText.toString());
    }
}
