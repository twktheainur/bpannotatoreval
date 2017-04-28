package org.pratikpharma.io.ehealth2017.corpus;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.regex.Pattern;


public class ICD10AnnotationImpl implements ICD10Annotation {

    private static final String CACHE_VALUE_SEPARATOR = "_____";
    private static final Pattern FIELD_SEPARATOR_PATTERN = Pattern.compile(CACHE_VALUE_SEPARATOR);
    private final String standardText;
    private final String icd10Code;
    private Integer causeRankFirst;
    private Integer causeRankSecond;

    public ICD10AnnotationImpl(final String standardText, final  String icd10Code) {
        this.standardText = standardText;
        this.icd10Code = icd10Code;
    }

    public ICD10AnnotationImpl(final CharSequence cacheValueLine) {
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

    }

    @Override
    public String getCauseRank(){
        String output = "";
        if((causeRankFirst != null) && (causeRankSecond != null)){
            output = String.format("%s-%s",causeRankFirst,causeRankSecond);
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
    public void setCauseRankFirst(final Integer causeRank){
        causeRankFirst =causeRank;
    }

    @Override
    public void setCauseRankSecond(final Integer causeRankSecond) {
        this.causeRankSecond = causeRankSecond;
    }

    @Override
    public String getCacheString() {
        return getIcd10Code() + CACHE_VALUE_SEPARATOR + getStandardText() + CACHE_VALUE_SEPARATOR + getCauseRankFirst() + CACHE_VALUE_SEPARATOR + getCauseRankSecond();
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if ((o == null) || (getClass() != o.getClass())) return false;

        final ICD10Annotation other = (ICD10Annotation) o;

        return new EqualsBuilder()
                .append(getIcd10Code(), other.getIcd10Code())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getIcd10Code())
                .toHashCode();
    }

    @Override
    public int compareTo(final ICD10Annotation other) {
        return icd10Code.compareTo(other.getIcd10Code());
    }
}
