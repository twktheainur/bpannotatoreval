package fr.lirmm.advanse.textprocessor;


import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.matetools.MatePosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.getalp.lexsema.util.Language;
import fr.lirmm.advanse.textprocessor.api.AbstractDKPTextProcessor;

public class GermanDKPTextProcessor extends AbstractDKPTextProcessor {
    public GermanDKPTextProcessor() {
        super(Language.GERMAN);
    }

    @Override
    protected AnalysisEngineDescription[] defineAnalysisEngine() throws ResourceInitializationException {
        AnalysisEngineDescription[] descriptors = new AnalysisEngineDescription[3];
        descriptors[0] = AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class);
        descriptors[1] = AnalysisEngineFactory.createEngineDescription(MatePosTagger.class);
        descriptors[2] = AnalysisEngineFactory.createEngineDescription(MateLemmatizer.class);
        return descriptors;
    }
}
