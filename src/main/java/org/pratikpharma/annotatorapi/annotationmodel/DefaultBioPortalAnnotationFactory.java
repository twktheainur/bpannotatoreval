package org.pratikpharma.annotatorapi.annotationmodel;

import org.pratikpharma.annotatorapi.api.BioPortalAnnotation;
import org.pratikpharma.annotatorapi.api.BioPortalAnnotationFactory;

public class    DefaultBioPortalAnnotationFactory implements BioPortalAnnotationFactory{

    @Override
    public BioPortalAnnotation createAnnotation(String text, String classId, String ontology, String type, String cuis, double score, int begin, int end) {
        return new DefaultBioPortalAnnotation(text,classId,ontology,type,cuis,score,begin,end);
    }
}
