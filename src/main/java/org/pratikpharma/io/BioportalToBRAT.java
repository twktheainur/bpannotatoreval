package org.pratikpharma.io;


import org.pratikpharma.annotatorapi.annotationmodel.BioPortalAnnotations;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BioportalToBRAT {
    private Map<String,BioPortalAnnotations> typeAnnotations = new HashMap<>();

    public void addAnnotationsForType(String type, BioPortalAnnotations annotations){
        typeAnnotations.put(type,annotations);
    }

    public void writeBRATFromAnnotator(Path output){

    }
}
