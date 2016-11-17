package org.pratikpharma.annotatorapi.api;


public interface BioPortalAnnotatorQuery {
    void addOntology(String ontologyName);

    String generateOntologyString();

    void addSemanticType(String semanticType);

    String generateSemanticTypesString();

    void setLongestOnly(boolean longestOnly);

    boolean isLongestOnly();

    String getText();

    void setText(String text);

    boolean isExcludeNumbers();

    void setExcludeNumbers(boolean excludeNumbers);

    boolean isWholeWordOnly();

    void setWholeWordOnly(boolean wholeWordOnly);

    boolean isExcludeSynonyms();

    void setExcludeSynonyms(boolean excludeSynonyms);

    boolean isExpandMappings();

    void setExpandMappings(boolean expandMappings);

    String getScore();

    void setScore(String score);

    boolean isExpandClassHierarchy();

    void setExpandClassHierarchy(boolean expandClassHierarchy);

    int getClassHierarchyMaxLevel();

    void setClassHierarchyMaxLevel(int classHierarchyMaxLevel);

    String getFormat();

    void setFormat(String format);
}
