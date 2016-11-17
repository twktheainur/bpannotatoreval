package org.pratikpharma.ehealthtask;


import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.similarity.Text;
import org.getalp.lexsema.util.Language;
import org.pratikpharma.annotatorapi.BioportalAnnotatorQueryBuilder;
import org.pratikpharma.annotatorapi.annotationmodel.BioPortalAnnotations;
import org.pratikpharma.annotatorapi.api.BioPortalAnnotation;
import org.pratikpharma.annotatorapi.api.BioPortalAnnotationFactory;
import org.pratikpharma.annotatorapi.api.BioPortalAnnotator;
import org.pratikpharma.annotatorapi.api.BioPortalAnnotatorQuery;
import org.pratikpharma.umls.cui.CUIRetrieval;
import org.pratikpharma.umls.groups.UMLSGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DirectQuaeroAnnotator implements TaskAnnotator {
    private static final Logger logger = LoggerFactory.getLogger(QuaeroTokenMappingAnnotator.class);
    private final BioPortalAnnotations bioPortalAnnotations;
    private final BioPortalAnnotator annotator;

    private Map<String, UMLSGroup> groups;

    public DirectQuaeroAnnotator(Map<String, UMLSGroup> groups, BioPortalAnnotator annotator, BioPortalAnnotationFactory factory, CUIRetrieval cuiRetrieval) {
        this.groups = groups;
        this.bioPortalAnnotations = new BioPortalAnnotations(factory, cuiRetrieval);
        this.annotator = annotator;
    }

    @Override
    public void annotateText(Text text, Path resultsDirectory) throws IOException {
        String textString = text.asString();
        annotateText(textString, text.getId(), text.getLanguage(), resultsDirectory);
    }

    @Override
    public void annotateText(String text, String textId, Language language, Path resultsDirectory) throws IOException {
        if (!Files.exists(resultsDirectory)) {
            Files.createDirectory(resultsDirectory);
        }
        Path outputFile = Paths.get(resultsDirectory.toString(), textId + ".ann");

        for (String groupType : groups.keySet()) {
            //logger.info("\n ************{}************** \n", groupType);
            //Crafting query

            BioPortalAnnotatorQuery query = BioportalAnnotatorQueryBuilder.DEFAULT
                    .text(text).semantic_types(groups.get(groupType).types()).expand_mappings(true).build();

            String output = annotator.runQuery(query);
            //logger.info(output);
            bioPortalAnnotations.parseJSONAnnotations(output, groupType);
            //logger.info(bioPortalAnnotations.annotations().toString());
        }
        writeBRAT(bioPortalAnnotations.annotations(), outputFile);

    }


    private void writeBRAT(List<BioPortalAnnotation> annotations, Path file) {
        Collections.sort(annotations, (o1, o2) -> Integer.compare(o1.getBegin(), o2.getBegin()));
        int termCounter = 1;
        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(file))) {
            for (BioPortalAnnotation annotation : annotations) {
                printWriter.println(String.format("T%d\t%s %d %d\t%s", termCounter, annotation.getType(), annotation.getBegin(), annotation.getEnd(), annotation.getText().toLowerCase()));
                printWriter.println(String.format("#%d\tAnnotatorNotes T%d\t%s", termCounter, termCounter, annotation.getCuis()));
                termCounter++;
            }
        } catch (IOException e) {
            logger.error("Cannot open file for writing: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void annotateCorpus(CorpusLoader corpusLoader, Path resultsDirectory) {
        for (Text text : corpusLoader) {
            try {
                annotateText(text, resultsDirectory);
            } catch (IOException e) {
                logger.error("Cannot create results directory");
            }
        }
    }
}
