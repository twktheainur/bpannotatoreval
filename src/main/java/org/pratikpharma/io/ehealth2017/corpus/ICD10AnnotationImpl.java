package org.pratikpharma.io.ehealth2017.corpus;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.pratikpharma.disambiguation.ontology.ClassSemanticInformation;
import redis.clients.jedis.Jedis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ICD10AnnotationImpl implements ICD10Annotation {

    private static final String CACHE_VALUE_SEPARATOR = "_____";
    private static final Pattern FIELD_SEPARATOR_PATTERN = Pattern.compile(CACHE_VALUE_SEPARATOR);
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    private final String uri;
    private final String standardText;
    private final String icd10Code;
    private Integer causeRankFirst;
    private Integer causeRankSecond;
    private double score = 0d;

    private ClassSemanticInformation classSemanticInformation;

    public ICD10AnnotationImpl(final String uri, final String standardText) {
        this.uri = uri;
        this.standardText = standardText;

        final Matcher matcher;
        if (uri.contains("#")) {
            matcher = DOT_PATTERN.matcher(uri.split("#")[1]);
        } else {
            final String[] fields = uri.split("/");
            matcher = DOT_PATTERN.matcher(fields[fields.length - 1]);
        }
        icd10Code = matcher.replaceAll("");
    }

    public ICD10AnnotationImpl(final String cacheValueLine) {
        final String[] fields = FIELD_SEPARATOR_PATTERN.split(cacheValueLine);
        icd10Code = fields[0];
        standardText = fields[1];
        if (fields.length > 2) {
            final String causeRankFirst = fields[2];
            final String causeRankSecond = fields[3];
            if (!causeRankFirst.equals("null") && !causeRankSecond.equals("null")) {
                setCauseRankFirst(Integer.valueOf(causeRankFirst));
                setCauseRankSecond(Integer.valueOf(causeRankSecond));
            }
        }
        uri = cacheValueLine.contains("URI" + CACHE_VALUE_SEPARATOR) ? fields[5] : "";
    }

    @Override
    public String getCauseRank() {
        String output = "";
        if ((causeRankFirst != null) && (causeRankSecond != null)) {
            output = String.format("%s-%s", causeRankFirst, causeRankSecond);
        }
        return output;
    }

    @Override
    public String getStandardText() {
        return standardText;
    }

    @Override
    public String getIcd10Code() {
        return icd10Code;
    }

    @Override
    public Integer getCauseRankFirst() {
        return causeRankFirst;
    }

    @Override
    public Integer getCauseRankSecond() {
        return causeRankSecond;
    }

    @Override
    public void setCauseRankFirst(final Integer causeRank) {
        causeRankFirst = causeRank;
    }

    @Override
    public void setCauseRankSecond(final Integer causeRankSecond) {
        this.causeRankSecond = causeRankSecond;
    }

    @Override
    public void cache(final Jedis jedis, final String annotationKey) {
        jedis.lpush(annotationKey, getCacheString());
    }


    private String getCacheString() {
        return getIcd10Code() + CACHE_VALUE_SEPARATOR +
                getStandardText() + CACHE_VALUE_SEPARATOR +
                getCauseRankFirst() + CACHE_VALUE_SEPARATOR +
                getCauseRankSecond() + CACHE_VALUE_SEPARATOR +
                "URI" + CACHE_VALUE_SEPARATOR + uri;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if ((o == null) || (getClass() != o.getClass())) return false;

        final ICD10Annotation other = (ICD10Annotation) o;

        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(getIcd10Code(), other.getIcd10Code());
        return equalsBuilder.isEquals();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 37);
        hashCodeBuilder.append(getIcd10Code());
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public int compareTo(final ICD10Annotation other) {
        return icd10Code.compareTo(other.getIcd10Code());
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setClassSemanticInformation(final ClassSemanticInformation classSemanticInformation) {
        this.classSemanticInformation = classSemanticInformation;
    }

    @Override
    public ClassSemanticInformation getClassSemanticInformation() {
        return classSemanticInformation;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public void setScore(final double score) {
        this.score = score;
    }
}
