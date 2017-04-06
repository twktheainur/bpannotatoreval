package org.pratikpharma.io.ehealt2017;

import org.pratikpharma.io.ehealt2017.corpus.*;
import org.pratikpharma.io.ehealt2017.corpus.enumerations.DocumentGender;
import org.pratikpharma.io.ehealt2017.corpus.enumerations.DocumentLocationOfDeath;
import org.pratikpharma.io.ehealt2017.corpus.enumerations.LineIntervalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EHealth2017Task1Reader implements Iterable<Document> {

    private static final Logger logger = LoggerFactory.getLogger(EHealth2017Task1Reader.class);

    private final String pathString;
    private final List<Document> corpus;


    public EHealth2017Task1Reader(final String path) {
        pathString = path;
        corpus = new ArrayList<>();
    }

    @SuppressWarnings({"MethodReturnOfConcreteClass", "PublicMethodNotExposedInInterface"})
    public EHealth2017Task1Reader load() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathString))) {
            final Map<Integer, Document> documentIndex = new HashMap<>();
            final Map<Document, Map<Integer,DocumentLine>> documentLineIndex = new HashMap<>();

            final Stream<String> lines = bufferedReader.lines();
            lines.forEach(new LineConsumer(documentIndex, documentLineIndex, corpus));

        } catch (final FileNotFoundException e) {
            logger.error("Cannot find corpus file: {}", e.getLocalizedMessage());
        } catch (final IOException e) {
            logger.error("Cannot read corpus file: {}", e.getLocalizedMessage());
        }
        return this;
    }


    @Override
    public void forEach(final Consumer<? super Document> action) {
        corpus.forEach(action);
    }

    @Override
    public Spliterator<Document> spliterator() {
        return corpus.spliterator();
    }

    @Override
    public Iterator<Document> iterator() {
        return corpus.iterator();
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    private static class LineConsumer implements Consumer<String> {
        private static final int ICD_COLUMN_INDEX = 11;
        private final Map<Integer, Document> documentIndex;
        private final Map<Document, Map<Integer,DocumentLine>> documentLineIndex;
        private final List<Document> corpus;

        LineConsumer(final Map<Integer, Document> documentIndex, final Map<Document, Map<Integer,DocumentLine>> documentLineIndex, final List<Document> corpus) {
            this.documentIndex = documentIndex;
            this.documentLineIndex = documentLineIndex;
            this.corpus = corpus;
        }

        @SuppressWarnings({"MethodWithMoreThanThreeNegations", "FeatureEnvy"})
        @Override
        public void accept(final String line) {
            final String[] fields = line.split(";");
            final int documentId = Integer.valueOf(fields[0]);
            final int yearCoded = Integer.valueOf(fields[1]);
            final DocumentGender gender = DocumentGender.getEnumValueFromCode(Integer.valueOf(fields[2]));
            final int age = Integer.valueOf(fields[3]);
            final DocumentLocationOfDeath documentLocationOfDeath = DocumentLocationOfDeath.getEnumValueFromCode(Integer.valueOf(fields[4]));
            final int lineId = Integer.valueOf(fields[5]);
            final String rawText = fields[6];

            if (!documentIndex.containsKey(documentId)) {
                final Document document = new DocumentImpl(documentId, yearCoded, age, gender, documentLocationOfDeath);
                documentIndex.put(documentId, document);
                corpus.add(document);

            }
            final Document document = documentIndex.get(documentId);

            if (!documentLineIndex.containsKey(document)) {
                documentLineIndex.put(document,new HashMap<>());
            }

            final Map<Integer,DocumentLine> currentLineMap = documentLineIndex.get(document);

            if(!currentLineMap.containsKey(lineId)){
                final DocumentLine documentLine = new DocumentLineImpl(lineId, document, rawText);
                currentLineMap.put(lineId, documentLine);
                document.addLine(documentLine);
            }

            final DocumentLine documentLine = currentLineMap.get(lineId);

            final String intTypeString = fields[7];
            final String intValueString = fields[8];

            if (!intTypeString.equals("NULL") && !intValueString.equals("NULL")) {
                documentLine.setIntervalType(
                        LineIntervalType.getEnumValueFromCode(Integer.valueOf(intTypeString)));
                documentLine.setIntervalValue(Integer.valueOf(intValueString));
            }

            if (fields.length > ICD_COLUMN_INDEX) {
                final ICD10Annotation icd10Annotation = new ICD10AnnotationImpl(fields[10], fields[ICD_COLUMN_INDEX]);
                if (!fields[9].isEmpty()) {
                    final String[] causeRanks = fields[9].split("-");
                    icd10Annotation.setCauseRankFirst(Integer.valueOf(causeRanks[0]));
                    icd10Annotation.setCauseRankSecond(Integer.valueOf(causeRanks[1]));
                }
                documentLine.addAnnotation(icd10Annotation);
            }
        }
    }
}
