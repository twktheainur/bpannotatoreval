package org.pratikpharma.disambiguation.similarity;

import com.wcohen.ss.AbstractStringDistance;
import org.pratikpharma.disambiguation.similarity.encoding.CodePointWrapper;
import org.pratikpharma.disambiguation.similarity.segmentation.Segmenter;
import org.pratikpharma.disambiguation.similarity.segmentation.SpaceSegmenter;

import java.util.List;

public class TverskiIndex implements SimilarityMeasure {
    private static final double APPROX_ONE = 0.999;
    private final Segmenter segmenter;

    private final double alpha;
    private final double beta;
    private final double gamma;
    private final boolean fuzzyMatching;
    private final boolean symmetric;
    private AbstractStringDistance distance;


    public TverskiIndex(final double alpha, final double beta, final double gamma, final boolean fuzzyMatching,
                        final boolean symmetric, final AbstractStringDistance distance) {
        this.distance = distance;
        segmenter = new SpaceSegmenter();
        this.alpha = alpha;
        this.beta = beta;
        this.fuzzyMatching = fuzzyMatching;
        this.symmetric = symmetric;
        this.gamma = gamma;
    }

    public TverskiIndex(final double alpha, final double beta, final double gamma, final boolean fuzzyMatching, final boolean symmetric) {
        segmenter = new SpaceSegmenter();
        this.alpha = alpha;
        this.beta = beta;
        this.fuzzyMatching = fuzzyMatching;
        this.symmetric = symmetric;
        this.gamma = gamma;
    }

    @SuppressWarnings("LocalVariableOfConcreteClass")
    private static int longestSubString(final String first, final String second) {
        int ret = 0;
        if ((first != null) && (second != null) && !(first.isEmpty()) && !(second.isEmpty())) {

            int maxLen = 0;
            final int fl = first.length();
            final int sl = second.length();
            final int[][] table = new int[fl][sl];
            final Iterable<Integer> cpFirst = new CodePointWrapper(first);
            int i = 0;
            for (final int cpi : cpFirst) {
                final Iterable<Integer> cpSecond = new CodePointWrapper(second);
                int j = 0;
                for (final int cpj : cpSecond) {
                    if (cpi == cpj) {
                        table[i][j] = ((i == 0) || (j == 0)) ? 1 : (table[i - 1][j - 1] + 1);
                        if (table[i][j] > maxLen) {
                            maxLen = table[i][j];
                        }
                    }
                    j++;
                }
                i++;
            }
            ret = maxLen;
        }
        return ret;
    }

    @Override
    public double compute(final String first, final String second) {
        return compute(segmenter.segment(first), segmenter.segment(second));
    }

    private double compute(final List<String> first, final List<String> second) {
        final double overlap = fuzzyMatching ? computeFuzzyOverlap(first, second) : computeOverlap(first, second);
        final double diffA = first.size() - overlap;
        final double diffB = second.size() - overlap;
        final double result;
        if (symmetric) {
            final double factA = Math.min(diffA, diffB);
            final double factB = Math.max(diffA, diffB);
            result = (alpha * overlap) / ((gamma * ((beta * factA) + ((1 - beta) * factB))) + (alpha * overlap));
        } else {
            result = (alpha * overlap) / (overlap + (diffA * beta) + (diffB * gamma));
        }
        return result;
    }

    private double computeOverlap(final List<String> first, final List<String> second) {
        final int size = Math.min(first.size(), second.size());
        double overlap = 0;
        for (int i = 0; (i < size) && first.get(i).contains(second.get(i)); i++) {
            overlap += 1;
        }
        return overlap;
    }

    private double computeFuzzyOverlap(final Iterable<String> la, final Iterable<String> lb) {
        double overlap = 0;
        for (final String first : la) {
            for (final String second : lb) {
                final double lcss = longestSubString(first, second);
                final double md = Math.max(Math.abs(lcss / first.length()), Math.abs(lcss / second.length()));
                final double score = (distance == null) ? md : distance.score(distance.prepare(first), distance.prepare(second));
                if ((score > APPROX_ONE) || ((score < 1.0) && ( (lcss >= 3)))) {
                    overlap += score;
                }
            }
        }

        return overlap;
    }

}
