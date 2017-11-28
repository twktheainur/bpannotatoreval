package org.pratikpharma.ehealthtask.quaero;

import org.json.simple.parser.ParseException;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;

import java.io.IOException;


@FunctionalInterface
public interface QaeroAnnotator {

    boolean annotateText(String text, String textId) throws IOException, NCBOAnnotatorErrorException, ParseException;
}
