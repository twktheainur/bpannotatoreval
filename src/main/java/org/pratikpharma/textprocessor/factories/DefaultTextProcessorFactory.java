package org.pratikpharma.textprocessor.factories;


import org.getalp.lexsema.util.Language;
import org.pratikpharma.textprocessor.EnglishDKPTextProcessor;
import org.pratikpharma.textprocessor.FrenchDKPTextProcessor;
import org.pratikpharma.textprocessor.GermanDKPTextProcessor;
import org.pratikpharma.textprocessor.RussianPythonTextProcessor;
import org.pratikpharma.textprocessor.api.TextProcessor;

public class DefaultTextProcessorFactory implements TextProcessorFactory {

    @Override
    public TextProcessor createProcessor(Language language) {
        TextProcessor processor;
        switch (language){
            case FRENCH:
                processor = new FrenchDKPTextProcessor();
                break;
            case GERMAN:
                processor = new GermanDKPTextProcessor();
                break;
            case RUSSIAN:
                processor = new RussianPythonTextProcessor();
                break;
            case ENGLISH:
            default:
                processor = new EnglishDKPTextProcessor();
                break;
        }
        return processor;
    }
}
