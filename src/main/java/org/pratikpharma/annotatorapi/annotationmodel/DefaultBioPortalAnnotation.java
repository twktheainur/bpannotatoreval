package org.pratikpharma.annotatorapi.annotationmodel;

import org.pratikpharma.annotatorapi.api.BioPortalAnnotation;

class DefaultBioPortalAnnotation implements BioPortalAnnotation {
    private String text;
    private String classId;
    private String ontology;
    private String type;
    private String cuis;
    private double score;
    private int begin;
    private int end;

    DefaultBioPortalAnnotation(String text, String classId, String ontology, String type, String cuis,double score, int begin, int end) {
        this.text = text;
        this.classId = classId;
        this.ontology = ontology;
        this.type = type;
        this.score = score;
        this.begin = begin;
        this.end = end;
        this.cuis = cuis;
    }

    @Override
    public String getText() {
        return text;
    }


    @Override
    public String getClassId() {
        return classId;
    }

    @Override
    public String getOntology() {
        return ontology;
    }



    @Override
    public String getType() {
        return type;
    }


    @Override
    public double getScore() {
        return score;
    }

    @Override
    public int getBegin() {
        return begin;
    }
    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "DefaultBioPortalAnnotation{" +
                "text='" + text + '\'' +
                ", classId='" + classId + '\'' +
                ", ontology='" + ontology + '\'' +
                ", type='" + type + '\'' +
                ", score=" + score +
                ", begin=" + begin +
                ", end=" + end +
                '}';
    }

    @Override
    public String getCuis() {
        return cuis;
    }
}
