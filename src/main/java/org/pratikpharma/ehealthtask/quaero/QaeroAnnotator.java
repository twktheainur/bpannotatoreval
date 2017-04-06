package org.pratikpharma.ehealthtask.quaero;

import org.json.simple.parser.ParseException;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;

import java.io.IOException;
import java.nio.file.Path;


@FunctionalInterface
public interface QaeroAnnotator {

    void annotateText(String text, String textId,  Path resultPath) throws IOException, NCBOAnnotatorErrorException, ParseException;
}
