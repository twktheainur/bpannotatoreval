package org.pratikpharma.cli;

import org.pratikpharma.io.QuaeroCorpusLoader;
import org.pratikpharma.textprocessor.EnglishDKPTextProcessor;
import org.pratikpharma.textprocessor.api.TextProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GenerateProcessedCorpus {

    private static final Logger logger = LoggerFactory.getLogger(GenerateProcessedCorpus.class);

    private GenerateProcessedCorpus() {
    }

    public static void main(String... args){
        TextProcessor textProcessor = new EnglishDKPTextProcessor();
        QuaeroCorpusLoader quaeroCorpusLoader = new QuaeroCorpusLoader("../data/quaero/corpus/test/MEDLINE",textProcessor);
        quaeroCorpusLoader.load();

    }
}
