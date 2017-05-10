package org.pratikpharma.disambiguation.ontology.queries;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import org.sparqy.api.Graph;
import org.sparqy.graph.DefaultGraph;
import org.sparqy.queries.ARQSelectQueryImpl;
import org.sparqy.queries.AbstractQueryProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrefLabelRetriever extends AbstractQueryProcessor<String> {

    private static final String LABEL_VAR = "lv";
    private final String conceptURI;
    private static final Graph graph = new DefaultGraph("", null);


    public PrefLabelRetriever(final String conceptURI) throws IOException {
        super(graph);
        this.conceptURI = conceptURI;
        initialize();
    }


    @Override
    protected void defineQuery() {
        setQuery(new ARQSelectQueryImpl());

        addTriple(NodeFactory.createURI(conceptURI),
                NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"),
                Var.alloc(LABEL_VAR));

        addResultVar(LABEL_VAR);
    }

    @Override
    public List<String> processResults() {
        final List<String> labels = new ArrayList<>();
        while (hasNextResult()) {
            final QuerySolution qs = nextSolution();
            final RDFNode resultUri = qs.get(LABEL_VAR);
            final Literal literal = resultUri.asLiteral();
            labels.add(literal.getString());
        }
        return labels;
    }
}
