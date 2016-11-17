package org.pratikpharma.umls.cui;


import lib.sparqy.Graph;
import lib.sparqy.graph.DefaultGraph;
import lib.sparqy.graph.OWLTBoxModel;
import lib.sparqy.queries.QueryProcessor;
import org.pratikpharma.umls.cui.querries.GetCUIQueryProcessor;

import java.io.IOException;
import java.util.List;

public final class CUIRetrieval {

    private Graph graph;

    public CUIRetrieval() throws IOException {
        graph = new DefaultGraph("http://purl.lirmm.fr/ontology/", new OWLTBoxModel());
    }

    public List<String> getCUIsForURI(String URI) {
        QueryProcessor<String> cuiProcessor = new GetCUIQueryProcessor(graph, URI);
        cuiProcessor.runQuery();
        return cuiProcessor.processResults();
    }
}
