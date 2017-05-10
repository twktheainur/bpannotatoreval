package org.pratikpharma.disambiguation.ontology;

import java.util.List;

public interface ClassSemanticInformation {
    String getTextualSignature();
    List<CUI> getCUIs();
    List<String> getTUIs();

    String getClassURI();
}
