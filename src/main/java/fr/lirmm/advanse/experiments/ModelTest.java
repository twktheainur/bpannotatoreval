package fr.lirmm.advanse.experiments;


import lib.sparqy.graph.storage.JenaRemoteSPARQLStore;
import lib.sparqy.graph.storage.StoreHandler;
import lib.sparqy.graph.store.Store;
import org.sifrproject.annotations.api.model.Annotation;
import org.sifrproject.annotations.api.model.AnnotationFactory;
import org.sifrproject.annotations.api.model.AnnotationParser;
import org.sifrproject.annotations.api.umls.PropertyRetriever;
import org.sifrproject.annotations.model.BioPortalAnnotationFactory;
import org.sifrproject.annotations.model.BioPortalAnnotationParser;
import org.sifrproject.annotations.umls.CUIPropertyRetriever;
import org.sifrproject.annotations.umls.SemanticTypePropertyRetriever;
import org.sifrproject.annotations.umls.groups.UMLSGroupIndex;
import org.sifrproject.annotations.umls.groups.UMLSSemanticGroupsLoader;
import org.sifrproject.annotatorclient.BioportalAnnotatorFactory;
import org.sifrproject.annotatorclient.BioportalAnnotatorQueryBuilder;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.sifrproject.annotatorclient.api.BioPortalAnnotatorQuery;

import java.io.IOException;
import java.util.List;

public class ModelTest {
    //http://data.bioontology.org/annotator
    //private static Logger logger = LoggerFactory.getLogger(AnnotatorBaseline.class);

    public static void main(String... args) throws IOException {

        //Setting Up Sparql Endpoint connection for CUI retrieval
        //Store store = new JenaRemoteSPARQLStore("http://localhost:8080/sparql");
        Store store = new JenaRemoteSPARQLStore("http://sparql.bioportal.lirmm.fr/sparql/");
        StoreHandler.registerStoreInstance(store);
        PropertyRetriever cuiRetrieval = new CUIPropertyRetriever();
        PropertyRetriever typeRetrieval = new SemanticTypePropertyRetriever();
        UMLSGroupIndex umlsGroupIndex = UMLSSemanticGroupsLoader.load();


        AnnotationFactory annotationFactory = new BioPortalAnnotationFactory();
        BioPortalAnnotator annotator = BioportalAnnotatorFactory.createDefaultAnnotator("http://services.bioportal.lirmm.fr/annotator", "907d47d9-3a00-4aa7-9111-090112dfab6a");

        AnnotationParser parser = new BioPortalAnnotationParser(annotationFactory,cuiRetrieval,typeRetrieval, umlsGroupIndex);

        BioPortalAnnotatorQuery query = BioportalAnnotatorQueryBuilder.DEFAULT.expand_mappings(true).score("cvalueh").class_hierarchy_max_level(5).build();

        String response = annotator.runQuery(query);
        parser.parseAnnotations(response);

        List<Annotation> annotations = parser.annotations();

        System.err.println(annotations);
    }
}
