package org.pratikpharma.io.ehealth2017.disambiguation;

import java.util.List;

public interface ClassSemanticInformation {
    String getTextualSignature();
    List<CUI> getCUIs();
}
