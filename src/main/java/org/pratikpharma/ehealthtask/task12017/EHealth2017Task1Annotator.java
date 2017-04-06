package org.pratikpharma.ehealthtask.task12017;


import org.json.simple.parser.ParseException;
import org.pratikpharma.io.ehealt2017.corpus.Document;
import org.pratikpharma.io.ehealt2017.corpus.DocumentLine;
import org.pratikpharma.io.ehealt2017.corpus.ICD10Annotation;
import org.pratikpharma.io.ehealt2017.corpus.ICD10AnnotationImpl;
import org.sifrproject.annotations.api.input.AnnotationParser;
import org.sifrproject.annotations.api.model.AnnotatedClass;
import org.sifrproject.annotations.api.model.Annotation;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotations.input.BioPortalJSONAnnotationParser;
import org.sifrproject.annotations.model.BioPortalLazyAnnotationFactory;
import org.sifrproject.annotatorclient.BioportalAnnotatorQueryBuilder;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.sifrproject.annotatorclient.api.BioPortalAnnotatorQuery;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("HardcodedLineSeparator")
public class EHealth2017Task1Annotator {

    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    private static final String CACHE_VALUE_SEPARATOR = "_____";
    private static final Pattern FIELD_SEPARATOR_PATTERN = Pattern.compile(CACHE_VALUE_SEPARATOR);
    public static final double PROGRESS_THRESHOLD = 0.01;
    private final BioPortalAnnotator annotator;

    private static final Pattern SPECIAL_CHARS = Pattern.compile("[\t\n\r]");

    private final AnnotationParser annotationParser;

    private final Jedis jedis;

    public EHealth2017Task1Annotator(final BioPortalAnnotator annotator, final Jedis jedis) {
        this.annotator = annotator;
        annotationParser = new BioPortalJSONAnnotationParser(new BioPortalLazyAnnotationFactory());
        this.jedis = jedis;
    }


    public void annotate(final Iterable<Document> corpus, final PrintWriter resultOutput) throws IOException, NCBOAnnotatorErrorException, ParseException {

        int totalLineCount = 0;
        for (final Document document : corpus) {
            totalLineCount += document.size();
        }

        final AtomicInteger progressCount = new AtomicInteger(0);

        double previousProgress = 0d;

        for (final Document document : corpus) {
            for (final DocumentLine documentLine : document) {

                final String text = documentLine.getRawText();
                if (text.trim().isEmpty()) {
                    resultOutput.println(resultLine(document, documentLine, null));
                } else {
                    final String annotationKey = String.format("%d_%d", document.getId(), documentLine.getLineId());

                    final List<String> cachedAnnotations = jedis.lrange(annotationKey, 0, -1);

                    if (cachedAnnotations.isEmpty()) {

                        final Matcher matcher = SPECIAL_CHARS.matcher(text);
                        final BioportalAnnotatorQueryBuilder queryBuilder = BioportalAnnotatorQueryBuilder.DEFAULT
                                .text(matcher.replaceAll(" ")).lemmatize(false).ontologies("CIM-10", "CIM-10DC")
                                .longest_only(true);

                        final BioPortalAnnotatorQuery query = queryBuilder.build();

                        final String output = annotator.runQuery(query);

                        final List<Annotation> annotations = annotationParser.parseAnnotations(output);

                        annotations.forEach(new AnnotationConsumer(documentLine));

                        if (documentLine.hasAnnotation()) {
                            for (final ICD10Annotation annotation : documentLine) {
                                jedis.lpush(annotationKey, annotation.getIcd10Code() + CACHE_VALUE_SEPARATOR + annotation.getStandardText() + CACHE_VALUE_SEPARATOR + annotation.getCauseRankFirst() + CACHE_VALUE_SEPARATOR + annotation.getCauseRankSecond());
                                resultOutput.println(resultLine(document, documentLine, annotation));
                            }
                        } else {
                            resultOutput.println(resultLine(document, documentLine, null));
                        }
                    } else {
                        for (final String annotationString : cachedAnnotations) {
                            final String[] fields = FIELD_SEPARATOR_PATTERN.split(annotationString);
                            final String code = fields[0];
                            final String standardText = fields[1];
                            final ICD10Annotation annotation = new ICD10AnnotationImpl(standardText, code);
                            if (fields.length > 2) {
                                final String causeRankFirst = fields[2];
                                final String causeRankSecond = fields[3];
                                if(!causeRankFirst.equals("null") && !causeRankSecond.equals("null")) {
                                    annotation.setCauseRankFirst(Integer.valueOf(causeRankFirst));
                                    annotation.setCauseRankSecond(Integer.valueOf(causeRankSecond));
                                }
                            }

                            resultOutput.println(resultLine(document, documentLine, annotation));
                        }
                    }
                }
            }

            final double progress = (progressCount.addAndGet(document.size()) / (double) totalLineCount) * 100;

            if (Math.abs(progress - previousProgress) > PROGRESS_THRESHOLD) {
                //noinspection UseOfSystemOutOrSystemErr,HardcodedLineSeparator
                System.out.print(String.format("\rAnnotating %.2f%%", progress));
                previousProgress = progress;
            }
        }
    }

    @SuppressWarnings({"LawOfDemeter", "FeatureEnvy"})
    private String resultLine(final Document document, final DocumentLine documentLine, final ICD10Annotation icd10Annotation) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%d;%d;%d;%d;%d;",
                document.getId(),
                document.getYearCoded(),
                document.getGender().getCode(),
                document.getAge(),
                document.getLocationOfDeath().getCode()));

        String intTypeString = "NULL";
        String intValueString = "NULL";

        if (documentLine.getIntervalType() != null) {
            intTypeString = String.valueOf(documentLine.getIntervalType().getCode());
            intValueString = String.valueOf(documentLine.getIntervalValue());
        }

        stringBuilder.append(String.format("%d;%s;%s;%s;",
                documentLine.getLineId(),
                documentLine.getRawText(),
                intTypeString,
                intValueString
        ));

        if (icd10Annotation == null) {
            stringBuilder.append(";;");
        } else {
            stringBuilder.append(String.format("%s;%s;%s", icd10Annotation.getCauseRank(), icd10Annotation.getStandardText(), icd10Annotation.getIcd10Code()));
        }

        return stringBuilder.toString();

    }

    private static class AnnotationConsumer implements Consumer<Annotation> {
        private final DocumentLine documentLine;

        AnnotationConsumer(final DocumentLine documentLine) {
            this.documentLine = documentLine;
        }

        @Override
        public void accept(final Annotation annotation) {
            final AnnotatedClass annotatedClass = annotation.getAnnotatedClass();
            final String id = annotatedClass.getId();
            final Matcher matcher = DOT_PATTERN.matcher(id.split("#")[1]);
            final String code = matcher.replaceAll("");
            final String standardText = annotation.getAnnotations().iterator().next().getText();
            documentLine.addAnnotation(new ICD10AnnotationImpl(standardText, code));
        }
    }
}
