package org.pratikpharma.disambiguation.ontology.queries;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import org.sparqy.api.Graph;
import org.sparqy.graph.DefaultGraph;
import org.sparqy.queries.ARQSelectQueryImpl;
import org.sparqy.queries.AbstractQueryProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CUIRetriever extends AbstractQueryProcessor<String> {

    private static final String CUI_VAR = "cui";
    private final String conceptURI;
    private static final Graph graph = new DefaultGraph("", null);


    public CUIRetriever(final String conceptURI) throws IOException {
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
        final List<String> labels = new ArrayList<>();
        while (hasNextResult()) {
            final QuerySolution qs = nextSolution();
            final RDFNode resultUri = qs.get(CUI_VAR);
            labels.add(resultUri.asLiteral().getString());
        }
        return labels;
    }
}
