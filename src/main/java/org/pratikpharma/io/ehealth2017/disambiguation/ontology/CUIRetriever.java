package org.pratikpharma.io.ehealth2017.disambiguation.ontology;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import org.sparqy.api.Graph;
import org.sparqy.queries.ARQSelectQueryImpl;
import org.sparqy.queries.AbstractQueryProcessor;

import java.util.ArrayList;
import java.util.List;

public class CUIRetriever extends AbstractQueryProcessor<String> {

    private static final String CUI_VAR = "cui";
    private final String conceptURI;


    public CUIRetriever(final Graph graph, final String conceptURI) {
        super(graph);
        this.conceptURI = conceptURI;
    }

    @Override
    protected void defineQuery() {
        setQuery(new ARQSelectQueryImpl());

        addTriple(NodeFactory.createURI(conceptURI),
                NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"),
                Var.alloc(CUI_VAR));

        addResultVar(CUI_VAR);
    }

    @Override
    public List<String> processResults() {
        final List<String> labels = new ArrayList<>();
        while (hasNextResult()) {
            final QuerySolution qs = nextSolution();
            final RDFNode resultUri = qs.get(CUI_VAR);
            labels.add(resultUri.asLiteral().getString());
        }
        return labels;
    }
}
