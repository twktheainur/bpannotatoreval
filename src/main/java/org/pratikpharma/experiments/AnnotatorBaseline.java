package org.pratikpharma.experiments;


import lib.sparqy.graph.storage.JenaRemoteSPARQLStore;
import lib.sparqy.graph.storage.StoreHandler;
import lib.sparqy.graph.store.Store;
import org.getalp.lexsema.util.Language;
import org.pratikpharma.annotatorapi.BioportalAnnotatorFactory;
import org.pratikpharma.annotatorapi.annotationmodel.DefaultBioPortalAnnotationFactory;
import org.pratikpharma.annotatorapi.api.BioPortalAnnotationFactory;
import org.pratikpharma.annotatorapi.api.BioPortalAnnotator;
import org.pratikpharma.ehealthtask.DirectQuaeroAnnotator;
import org.pratikpharma.ehealthtask.TaskAnnotator;
import org.pratikpharma.io.QuaeroRawReader;
import org.pratikpharma.umls.cui.CUIRetrieval;
import org.pratikpharma.umls.groups.UMLSGroup;
import org.pratikpharma.umls.groups.UMLSSemanticGroupsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class AnnotatorBaseline {
    //parameters.add(new PairImpl<>("apikey","1de0a270-29c5-4dda-b043-7c3580628cd5"));
    //parameters.add(new PairImpl<>("apikey","33cf147d-f54a-4e70-ac32-139af89b609e"));
    //http://data.bioontology.org/annotator
    private static Logger logger = LoggerFactory.getLogger(AnnotatorBaseline.class);

    public static void main(String... args) throws IOException {

        //Setting Up Sparql Endpoint connection for CUI retrieval
        Store store = new JenaRemoteSPARQLStore("http://localhost:8080/sparql");
        StoreHandler.registerStoreInstance(store);
        CUIRetrieval cuiRetrieval = new CUIRetrieval();


        Iterable<Map.Entry<String,String>> quaeroCorpus = new QuaeroRawReader("../data/quaero/corpus/test/EMEA").load();

        //Loading type groups

        Map<String, UMLSGroup> groups = UMLSSemanticGroupsLoader.load(AnnotatorBaseline.class.getResourceAsStream("/semgroups.ssv"));

        BioPortalAnnotationFactory annotationFactory = new DefaultBioPortalAnnotationFactory();
        BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://services.bioportal.lirmm.fr/annotator", "907d47d9-3a00-4aa7-9111-090112dfab6a");

        for( Map.Entry<String,String> textEntry : quaeroCorpus) {
            TaskAnnotator taskAnnotator = new DirectQuaeroAnnotator(groups, annotator, annotationFactory,cuiRetrieval);
            taskAnnotator.annotateText(textEntry.getValue(), textEntry.getKey(), Language.FRENCH, Paths.get("./resultsTest"));
        }
        //logger.info(processedText.toString());
    }
}
