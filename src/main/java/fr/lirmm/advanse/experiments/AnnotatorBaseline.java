package fr.lirmm.advanse.experiments;


import fr.lirmm.advanse.ehealthtask.DirectQuaeroAnnotator;
import fr.lirmm.advanse.ehealthtask.TaskAnnotator;
import fr.lirmm.advanse.io.QuaeroRawReader;
import lib.sparqy.graph.storage.JenaRemoteSPARQLStore;
import lib.sparqy.graph.storage.StoreHandler;
import lib.sparqy.graph.store.Store;
import org.getalp.lexsema.util.Language;
import org.sifrproject.annotations.api.umls.PropertyRetriever;
import org.sifrproject.annotations.model.BioPortalAnnotationFactory;
import org.sifrproject.annotations.umls.CUIPropertyRetriever;
import org.sifrproject.annotations.umls.SemanticTypePropertyRetriever;
import org.sifrproject.annotations.umls.groups.UMLSGroupIndex;
import org.sifrproject.annotations.umls.groups.UMLSSemanticGroupsLoader;
import org.sifrproject.annotatorclient.BioportalAnnotatorFactory;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class AnnotatorBaseline {
    //http://data.bioontology.org/annotator
    //private static Logger logger = LoggerFactory.getLogger(AnnotatorBaseline.class);

    public static void main(String... args) throws IOException {

        //Setting Up Sparql Endpoint connection for CUI retrieval
        //Store store = new JenaRemoteSPARQLStore("http://localhost:8080/sparql");
        Store store = new JenaRemoteSPARQLStore("http://sparql.bioportal.lirmm.fr/sparql/");
        StoreHandler.registerStoreInstance(store);
        PropertyRetriever cuiRetrieval = new CUIPropertyRetriever();
        PropertyRetriever typeRetrieval = new SemanticTypePropertyRetriever();


        Iterable<Map.Entry<String,String>> quaeroCorpus = new QuaeroRawReader("../data/quaero/corpus/test/EMEA").load();

        //Loading type groups

        UMLSGroupIndex groupIndex = UMLSSemanticGroupsLoader.load(AnnotatorBaseline.class.getResourceAsStream("/semgroups.ssv"));

       BioPortalAnnotationFactory annotationFactory = new BioPortalAnnotationFactory();
        BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://services.bioportal.lirmm.fr/annotator", "907d47d9-3a00-4aa7-9111-090112dfab6a");

        for( Map.Entry<String,String> textEntry : quaeroCorpus) {
            TaskAnnotator taskAnnotator = new DirectQuaeroAnnotator(groupIndex ,annotator, annotationFactory,cuiRetrieval, typeRetrieval);
            taskAnnotator.annotateText(textEntry.getValue(), textEntry.getKey(), Language.FRENCH, Paths.get("./resultsTest"));
        }
        //logger.info(processedText.toString());
    }
}
