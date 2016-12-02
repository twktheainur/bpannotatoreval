package fr.lirmm.advanse.textprocessor.factories;


import fr.lirmm.advanse.textprocessor.EnglishDKPTextProcessor;
import fr.lirmm.advanse.textprocessor.FrenchDKPTextProcessor;
import fr.lirmm.advanse.textprocessor.api.TextProcessor;
import org.getalp.lexsema.util.Language;
import fr.lirmm.advanse.textprocessor.GermanDKPTextProcessor;
import fr.lirmm.advanse.textprocessor.RussianPythonTextProcessor;

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
