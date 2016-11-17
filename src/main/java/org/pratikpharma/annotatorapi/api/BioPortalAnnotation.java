package org.pratikpharma.annotatorapi.api;


public interface BioPortalAnnotation {
    String getClassId();
    String getOntology();
    String getType();
    String getText();
    double getScore();
    int getBegin();
    int getEnd();
    String getCuis();
}
