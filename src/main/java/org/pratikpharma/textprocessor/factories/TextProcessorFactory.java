package org.pratikpharma.textprocessor.factories;


import org.getalp.lexsema.util.Language;
import org.pratikpharma.textprocessor.api.TextProcessor;

public interface TextProcessorFactory {
    TextProcessor createProcessor(Language language);
}
