package org.pratikpharma.ehealthtask;

import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.similarity.Text;
import org.getalp.lexsema.util.Language;

import java.io.IOException;
import java.nio.file.Path;


public interface TaskAnnotator {
    void annotateText(Text text, Path resultsDirectory) throws IOException;

    void annotateText(String text, String textId, Language language,  Path resultsDirectory) throws IOException;

    void annotateCorpus(CorpusLoader corpusLoader, Path resultsDirectory);
}
