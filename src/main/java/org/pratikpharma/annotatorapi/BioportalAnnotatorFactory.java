package org.pratikpharma.annotatorapi;


import org.pratikpharma.annotatorapi.api.BioPortalAnnotator;


public class BioportalAnnotatorFactory {
    public static BioPortalAnnotator createDefaultAnnotator(String uri, String apiKey){
        return new DefaultBioPortalAnnotator(uri,apiKey);
    }
}
