package org.pratikpharma.ehealthtask;

import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.similarity.Text;
import org.getalp.lexsema.util.Language;
import org.json.simple.parser.ParseException;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;

import java.io.IOException;
import java.nio.file.Path;


public interface TaskAnnotator {
    void annotateText(Text text, Path resultsDirectory) throws IOException, NCBOAnnotatorErrorException, ParseException;

    void annotateText(String text, String textId, Language language,  Path resultsDirectory) throws IOException, NCBOAnnotatorErrorException, ParseException;

    void annotateCorpus(CorpusLoader corpusLoader, Path resultsDirectory) throws NCBOAnnotatorErrorException, ParseException;
}
