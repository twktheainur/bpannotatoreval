package org.pratikpharma.ehealthtask;

import org.json.simple.parser.ParseException;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;

import java.io.IOException;
import java.nio.file.Path;


public interface TaskAnnotator {

    void annotateText(String text, String textId,  Path resultsDirectory) throws IOException, NCBOAnnotatorErrorException, ParseException;
}
