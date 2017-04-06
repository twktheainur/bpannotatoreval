package org.pratikpharma.io.ehealt2017.corpus;


public interface ICD10Annotation extends Comparable<ICD10Annotation> {
    String getCauseRank();

    String getStandardText();

    String getIcd10Code();

    Integer getCauseRankFirst();

    Integer getCauseRankSecond();

    void setCauseRankFirst(Integer causeRank);

    void setCauseRankSecond(Integer causeRankSecond);
}
