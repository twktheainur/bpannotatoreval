package org.pratikpharma.ehealthtask.task12017;


import org.json.simple.parser.ParseException;
import org.pratikpharma.io.ehealth2017.corpus.Document;
import org.pratikpharma.io.ehealth2017.corpus.DocumentLine;
import org.pratikpharma.io.ehealth2017.corpus.ICD10Annotation;
import org.pratikpharma.io.ehealth2017.corpus.ICD10AnnotationImpl;
import org.pratikpharma.util.EmptyResultsCache;
import org.sifrproject.annotations.api.input.AnnotationParser;
import org.sifrproject.annotations.api.model.AnnotatedClass;
import org.sifrproject.annotations.api.model.Annotation;
import org.sifrproject.annotations.exceptions.InvalidFormatException;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotations.input.BioPortalJSONAnnotationParser;
import org.sifrproject.annotations.model.BioPortalLazyAnnotationFactory;
import org.sifrproject.annotatorclient.BioportalAnnotatorQueryBuilder;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.sifrproject.annotatorclient.api.BioPortalAnnotatorQuery;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("HardcodedLineSeparator")
public class EHealth2017Task1Annotator {

    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    private static final double PROGRESS_THRESHOLD = 0.01;
    private static final double FREQ_CUTOFF_THRESHOLD = 0.750d;
    private final BioPortalAnnotator annotator;
    private final List<String> alCode;
    private final List<Integer> alFreq;
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[\t\n\r]");

    private final AnnotationParser annotationParser;

    private final Jedis jedis;

    private final String cacheKeyPrefix;

    private final MFCTypes mfc;

    public EHealth2017Task1Annotator(final BioPortalAnnotator annotator, final Jedis jedis, final String cacheKeyPrefix, final MFCTypes mfc) throws IOException {
        this.annotator = annotator;
        annotationParser = new BioPortalJSONAnnotationParser(new BioPortalLazyAnnotationFactory());
        this.jedis = jedis;
        this.cacheKeyPrefix = cacheKeyPrefix;
        this.mfc = mfc;
        // charger les fréquences des codes
        alCode = new ArrayList<>();
        alFreq = new ArrayList<>();

        try (InputStream inputStream = EHealth2017Task1Annotator.class.getResourceAsStream("/codefreq_train.csv")) {
            try (final BufferedReader r = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = r.readLine();
                while (line != null) {
                    alCode.add(line.split(";")[1]);
                    alFreq.add(Integer.parseInt(line.split(";")[0]));
                    line = r.readLine();
                }
            }
        }
    }

    @SuppressWarnings({"LocalVariableOfConcreteClass", "PublicMethodNotExposedInInterface"})
    public void annotate(final Iterable<Document> corpus, final PrintWriter resultOutput, final String... ontologies) throws IOException, NCBOAnnotatorErrorException, ParseException, InvalidFormatException {

        //Counting the number of line in order to calculate the progress of the annotation
        int totalLineCount = 0;
        for (final Document document : corpus) {
            totalLineCount += document.size();
        }

        final AtomicInteger progressCount = new AtomicInteger(0);
        double previousProgress = 0d;

        for (final Document document : corpus) {
            for (final DocumentLine documentLine : document) {
                final String text = documentLine.getRawText();
                if (!text.trim().isEmpty()) {
                    //Generating cache key for the annotations
                    final String annotationKey = generateLineCacheKey(document, documentLine);

                    //Fetching cached annotations
                    final List<String> cachedAnnotations = jedis.lrange(annotationKey, 0, -1);

                    //If there are no annotations in the cache, we fetch the annotations from Bioportal
                    if (cachedAnnotations.isEmpty() && !EmptyResultsCache.isEmpty(annotationKey, jedis)) {

                        final List<Annotation> annotations = getBioportalAnnotations(text, ontologies);
                        annotations.forEach(new AnnotationConsumer(documentLine));

                        if (documentLine.hasAnnotation()) {
                            for (final ICD10Annotation annotation : documentLine) {
                                cacheAnnotation(annotationKey, annotation);
                            }
                        } else {
                            EmptyResultsCache.markEmpty(annotationKey, jedis);
                        }

                        //We founnd annotations in the cache, we can now parse and load them
                    } else if (!cachedAnnotations.isEmpty()) {
                        loadCachedAnnotations(documentLine, cachedAnnotations);
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

        for (final Document document : corpus) {
            for (final DocumentLine documentLine : document) {
                if (documentLine.hasAnnotation()) {
                    final String annotationKey = generateLineCacheKey(document, documentLine);
                    processNewAnnotations(resultOutput, document, documentLine, annotationKey);
                } else {
                    resultOutput.println(resultLine(document, documentLine, null));
                }
            }
        }

    }

    private String generateLineCacheKey(final Document document, final DocumentLine documentLine) {
        return String.format("%s_%d_%d", cacheKeyPrefix, document.getId(), documentLine.getLineId());
    }

    private void cacheAnnotation(final String annotationKey, final ICD10Annotation annotation) {
        jedis.lpush(annotationKey, annotation.getCacheString());
    }

    private void loadCachedAnnotations(final DocumentLine documentLine, final Iterable<String> cachedAnnotations) {
        for (final String annotationString : cachedAnnotations) {
            final ICD10Annotation annotation = new ICD10AnnotationImpl(annotationString);
            documentLine.addAnnotation(annotation);
            //printWriter.println(resultLine(document, documentLine, annotation));
        }
    }

    private List<Annotation> getBioportalAnnotations(final CharSequence text, final String[] ontologies) throws IOException, ParseException, InvalidFormatException, NCBOAnnotatorErrorException {
        final Matcher matcher = SPECIAL_CHARS.matcher(text);
        final BioportalAnnotatorQueryBuilder queryBuilder = BioportalAnnotatorQueryBuilder.DEFAULT
                .text(matcher.replaceAll(" ")).lemmatize(false).ontologies(ontologies)
                .longest_only(true);

        final BioPortalAnnotatorQuery query = queryBuilder.build();
        final String output = annotator.runQuery(query);
        return annotationParser.parseAnnotations(output);

    }

    @SuppressWarnings("LocalVariableOfConcreteClass")
    private void processNewAnnotations(final PrintWriter printWriter, final Document document, final DocumentLine documentLine, final String annotationKey) {
        final List<CodeFreqPair> codeByFreq = new ArrayList<>();
        final double sum = computeFrequencies(documentLine, codeByFreq);
        //Normalizing code frequency distribution for current annotations
        for (final CodeFreqPair codeFreqPair : codeByFreq) {
            codeFreqPair.setFreq(codeFreqPair.getFreq() / sum);
        }

        if (codeByFreq.isEmpty() || (mfc == MFCTypes.NONE)) {
            for (final ICD10Annotation annotation : documentLine) {
                cacheAnnotation(annotationKey, annotation);
                printWriter.println(resultLine(document, documentLine, annotation));
            }
        } else if (!codeByFreq.isEmpty()) {
            handleMfcHeuristic(printWriter, document, documentLine, codeByFreq);
        }
    }

    @SuppressWarnings("LocalVariableOfConcreteClass")
    private void handleMfcHeuristic(final PrintWriter printWriter, final Document document, final DocumentLine documentLine, final List<CodeFreqPair> codeByFreq) {

        codeByFreq.sort((o1, o2) -> Double.compare(o2.getFreq(), o1.getFreq()));
        final Iterator<CodeFreqPair> iterator = codeByFreq.iterator();
        if (mfc == MFCTypes.CUTOFF) {
            double cumulativeFreq = 0d;
            while ((cumulativeFreq < FREQ_CUTOFF_THRESHOLD) && iterator.hasNext()) {
                final CodeFreqPair pair = iterator.next();
                cumulativeFreq += pair.getFreq();
                final ICD10Annotation annotation = pair.getAnnotation();
                printWriter.println(resultLine(document, documentLine, annotation));
            }
        } else if (mfc == MFCTypes.FIRST) {
            final ICD10Annotation annotation = iterator.next().getAnnotation();
            printWriter.println(resultLine(document, documentLine, annotation));
        }
    }

    private double computeFrequencies(final Iterable<ICD10Annotation> documentLine, final Collection<CodeFreqPair> frequencies) {
        double sum = 0d;
        for (final ICD10Annotation annotation : documentLine) {
            final int index = alCode.indexOf(annotation.getIcd10Code());
            if (index >= 0) {
                final double freq = alFreq.get(index);
                frequencies.add(new CodeFreqPair(annotation, freq));
                sum += freq;
            }
        }
        return sum;
    }

    @SuppressWarnings("all")
    private static class CodeFreqPair {
        private final ICD10Annotation annotation;
        private double freq;

        CodeFreqPair(final ICD10Annotation annotation, final double freq) {
            this.annotation = annotation;
            this.setFreq(freq);
        }

        public ICD10Annotation getAnnotation() {
            return annotation;
        }

        public double getFreq() {
            return freq;
        }

        public void setFreq(double freq) {
            this.freq = freq;
        }
    }

    @SuppressWarnings({"LawOfDemeter", "FeatureEnvy"})
    private String resultLine(final Document document, final DocumentLine documentLine,
                              final ICD10Annotation icd10Annotation) {
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
