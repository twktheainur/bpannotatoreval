package fr.lirmm.advanse.textprocessor;


import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.getalp.lexsema.util.Language;
import fr.lirmm.advanse.textprocessor.api.AbstractDKPTextProcessor;

public class FrenchDKPTextProcessor extends AbstractDKPTextProcessor {

    public FrenchDKPTextProcessor() {
        super(Language.FRENCH);
    }

    @Override
    protected AnalysisEngineDescription[] defineAnalysisEngine() throws ResourceInitializationException {
        AnalysisEngineDescription[] descriptors = new AnalysisEngineDescription[3];
        descriptors[0] = AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class);
        descriptors[1] = AnalysisEngineFactory.createEngineDescription(StanfordPosTagger.class);
        descriptors[2] = AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class);
        return descriptors;
    }
}
