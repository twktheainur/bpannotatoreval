package org.pratikpharma.umls.cui.querries;


import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import lib.sparqy.Graph;
import lib.sparqy.queries.ARQSelectQueryImpl;
import lib.sparqy.queries.AbstractQueryProcessor;

import java.util.ArrayList;
import java.util.List;


public class GetCUIQueryProcessor extends AbstractQueryProcessor<String> {

    private String conceptURI;
    private final String CUI_VAR = "cui";

    public GetCUIQueryProcessor(Graph graph, String conceptURI) {
        super(graph);
        this.conceptURI = conceptURI;
        initialize();
    }

    @Override
    protected void defineQuery() {


        setQuery(new ARQSelectQueryImpl());

        addTriple(NodeFactory.createURI(conceptURI),
                NodeFactory.createURI("http://bioportal.bioontology.org/ontologies/umls/cui"),
                Var.alloc(CUI_VAR));

        addResultVar(CUI_VAR);
    }

    @Override
    public List<String> processResults() {
        List<String> cuis = new ArrayList<>();
        while (hasNextResult()) {
            QuerySolution qs = nextSolution();
            RDFNode resultUri = qs.get(CUI_VAR);
            cuis.add(resultUri.asLiteral().getString());
        }
        return cuis;
    }
}
