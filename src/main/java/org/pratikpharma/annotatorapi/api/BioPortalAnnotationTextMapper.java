package org.pratikpharma.annotatorapi.api;

import org.getalp.lexsema.similarity.Text;
import org.getalp.lexsema.similarity.Word;
import org.getalp.lexsema.similarity.annotation.Annotations;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class BioPortalAnnotationTextMapper {

    private BioPortalAnnotationTextMapper() {
    }

    public static void transferAnnotations(Collection<BioPortalAnnotation> annotations, Text text) {
        for (BioPortalAnnotation bioPortalAnnotation : annotations) {
            List<Word> matchingWords = text.words().stream()
                    .filter(word -> word.getBegin() == bioPortalAnnotation.getBegin() &&
                            word.getEnd() == bioPortalAnnotation.getEnd())
                    .collect(Collectors.toList());
            matchingWords.forEach(word -> word.addAnnotation(
                    Annotations.createAnnotation(
                            bioPortalAnnotation.getClassId(),
                            bioPortalAnnotation.getType(),
                            bioPortalAnnotation.getOntology()
                    )
            ));
        }
    }
}
