package org.pratikpharma.ehealthtask;


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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectQuaeroAnnotator implements TaskAnnotator {
    private static final Logger logger = LoggerFactory.getLogger(DirectQuaeroAnnotator.class);
    @SuppressWarnings("HardcodedLineSeparator")
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[\t\n\r]");
    private final BioPortalAnnotator annotator;
    private final UMLSGroupIndex groupIndex;

    private String[] ontologies;
    private String[] semanticGroups;
    private final String format;
    private final boolean expandMappings;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    public DirectQuaeroAnnotator(final BioPortalAnnotator annotator, final UMLSGroupIndex groupIndex, final String[] ontologies, final String[] semanticGroups, final String format, final boolean expandMappings) {
        this.annotator = annotator;
        this.groupIndex = groupIndex;
        if(ontologies!=null) {
            this.ontologies = Arrays.copyOf(ontologies, ontologies.length);
        }
        if(semanticGroups !=null) {
            this.semanticGroups = Arrays.copyOf(semanticGroups, semanticGroups.length);
        }
        this.format = format;
        this.expandMappings = expandMappings;
    }

    @Override
    public void annotateText(final String text, final String textId, final Path resultsDirectory) throws IOException, NCBOAnnotatorErrorException, ParseException {
        if (!Files.exists(resultsDirectory)) {
            Files.createDirectory(resultsDirectory);
        }
        final Path outputFile = Paths.get(resultsDirectory.toString(), textId + ".ann");
        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(outputFile))) {


            if((semanticGroups == null) || (semanticGroups.length == 0)) {
                int numberOfGroups = 0;
                for (final String ignored : groupIndex) {
                    numberOfGroups++;
                }
                semanticGroups = new String[numberOfGroups];
                int currentGroup = 0;
                for (final String groupType : groupIndex) {
                    semanticGroups[currentGroup] = groupType;
                    currentGroup++;
                }
            }
            //logger.info("\n ************{}************** \n", groupType);
            //Crafting query

            final Matcher matcher = SPECIAL_CHARS.matcher(text);

            BioportalAnnotatorQueryBuilder queryBuilder =BioportalAnnotatorQueryBuilder.DEFAULT
                    .text(matcher.replaceAll(" ")).expand_mappings(expandMappings)
                    .format(format).semantic_groups(semanticGroups).lemmatize(false);

            if((ontologies != null) && (ontologies.length > 0)){
                queryBuilder = queryBuilder.ontologies(ontologies);
            }

            final BioPortalAnnotatorQuery query = queryBuilder.build();

            final String output = annotator.runQuery(query);
            printWriter.print(output);
            //logger.info(output);
            //writeBRAT(annitationParser.parseAnnotations(output), printWriter);
            //logger.info(annitationParser.annotations().toString());
            printWriter.flush();
            printWriter.close();
        }

    }
}
