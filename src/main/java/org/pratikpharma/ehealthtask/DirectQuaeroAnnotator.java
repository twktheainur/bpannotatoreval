package org.pratikpharma.ehealthtask;


import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.similarity.Text;
import org.getalp.lexsema.util.Language;
import org.json.simple.parser.ParseException;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotations.umls.UMLSGroupIndex;
import org.sifrproject.annotatorclient.BioportalAnnotatorQueryBuilder;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.sifrproject.annotatorclient.api.BioPortalAnnotatorQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectQuaeroAnnotator implements TaskAnnotator {
    private static final Logger logger = LoggerFactory.getLogger(DirectQuaeroAnnotator.class);
    private final BioPortalAnnotator annotator;
    private final UMLSGroupIndex groupIndex;


    public DirectQuaeroAnnotator(final UMLSGroupIndex groupIndex, final BioPortalAnnotator annotator) {
        this.groupIndex = groupIndex;
        this.annotator = annotator;
    }

    @Override
    public void annotateText(final Text text, final Path resultsDirectory) throws IOException, NCBOAnnotatorErrorException, ParseException {
        final String textString = text.asString();
        annotateText(textString, text.getId(), text.getLanguage(), resultsDirectory);
    }

    @Override
    public void annotateText(final String text, final String textId, final Language language, final Path resultsDirectory) throws IOException, NCBOAnnotatorErrorException, ParseException {
        if (!Files.exists(resultsDirectory)) {
            Files.createDirectory(resultsDirectory);
        }
        final Path outputFile = Paths.get(resultsDirectory.toString(), textId + ".ann");
        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(outputFile))) {

            final StringBuilder semanticGroups = new StringBuilder();
            boolean first = true;
            for (final String groupType : groupIndex) {
                semanticGroups.insert(0, groupType);
                if (first) {
                    first = false;
                } else {
                    semanticGroups.insert(0, ",");
                }
            }
            //logger.info("\n ************{}************** \n", groupType);
            //Crafting query

            String cleanText = text.replaceAll(System.lineSeparator()," ");
            cleanText = text.replaceAll("\t"," ");
            cleanText = text.replaceAll("\n"," ");
            cleanText = text.replaceAll("\r"," ");

            final BioPortalAnnotatorQuery query = BioportalAnnotatorQueryBuilder.DEFAULT
                    .text(cleanText).semantic_groups("DISO").expand_mappings(true)
                    .format("quaeroimg").ontologies("MSHFRE", "MDRFRE").lemmatize(false).build();

            final String output = annotator.runQuery(query);
            printWriter.print(output);
            //logger.info(output);
            //writeBRAT(annitationParser.parseAnnotations(output), printWriter);
            //logger.info(annitationParser.annotations().toString());
            printWriter.flush();
            printWriter.close();
        }

    }


    /*private void writeBRAT(final Iterable<Annotation> annotations, final PrintWriter printWriter) {
        final List<AnnotationToken> tokens = new ArrayList<>();
        final List<AnnotatedClass> annotatedClasses = new ArrayList<>();
        annotations.forEach(annotation -> annotation.getAnnotations().forEach(token -> {
            tokens.add(token);
            annotatedClasses.add(annotation.getAnnotatedClass());

        }));
        int termCounter = 1;
        for (int i = 0; i < tokens.size(); i++) {
            final AnnotationToken token = tokens.get(i);
            final AnnotatedClass annotatedClass = annotatedClasses.get(i);
            final String semanticGroup = annotatedClass.getSemanticGroups().iterator().next().name();
            final StringBuilder cuiBuilder = new StringBuilder();
            boolean first = true;
            for (final String cui : annotatedClass.getCuis()) {
                cuiBuilder.insert(0, cui);
                if (first) {
                    first = false;
                } else {
                    cuiBuilder.insert(0, ",");
                }
            }

            printWriter.println(String.format("T%d\t%s %d %d\t%s", termCounter, semanticGroup, token.getFrom(), token.getTo(), token.getText().toLowerCase()));
            printWriter.println(String.format("#%d\tAnnotatorNotes T%d\t%s", termCounter, termCounter, cuiBuilder.toString()));
            termCounter++;
        }
    }*/

    @Override
    public void annotateCorpus(final CorpusLoader corpusLoader, final Path resultsDirectory) throws NCBOAnnotatorErrorException, ParseException {
        for (final Text text : corpusLoader) {
            try {
                annotateText(text, resultsDirectory);
            } catch (final IOException e) {
                logger.error("Cannot create results directory");
            }
        }
    }
}
