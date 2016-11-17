package org.pratikpharma.textprocessor;


import org.getalp.lexsema.similarity.*;
import org.getalp.lexsema.util.Language;
import org.pratikpharma.textprocessor.api.TextProcessor;

public class SpaceSegmentingTextProcessor implements TextProcessor {

    private static final DocumentFactory DOCUMENT_FACTORY = DefaultDocumentFactory.DEFAULT;

    @Override
    public Text process(String sentenceText, String documentId) {
        String[] tokens = sentenceText.split(" ");
        Text text = DOCUMENT_FACTORY.createText(Language.ENGLISH);
        Sentence sentence = DOCUMENT_FACTORY.createSentence(documentId);
        for(String token: tokens){
            Word word = DOCUMENT_FACTORY.createWord("",token,token,"");
            text.addWord(word);
            sentence.addWord(word);
        }
        text.addSentence(sentence);
        return text;
    }
}
