package org.pratikpharma.io.ehealth2017.disambiguation.ontology;

import org.pratikpharma.io.ehealth2017.disambiguation.ClassSemanticInformation;

import java.util.List;

public interface SemanticInformationRetriver {
    ClassSemanticInformation getClassSemanticInformation(String uri);
    List<ClassSemanticInformation> getHierarchyClassSemanticInformation(String uri);
}
