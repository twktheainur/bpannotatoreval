package fr.lirmm.advanse.textprocessor.factories;


import fr.lirmm.advanse.textprocessor.api.TextProcessor;
import org.getalp.lexsema.util.Language;

public interface TextProcessorFactory {
    TextProcessor createProcessor(Language language);
}
