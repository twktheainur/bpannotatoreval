package org.pratikpharma.annotatorapi.api;

public interface BioPortalAnnotationFactory {
    BioPortalAnnotation createAnnotation(String text, String classId, String ontology, String type, String cuis, double score, int begin, int end);
}
