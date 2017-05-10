package org.pratikpharma.disambiguation.ontology;

import org.pratikpharma.disambiguation.ontology.queries.AltLabelRetriever;
import org.pratikpharma.disambiguation.ontology.queries.CUIRetriever;
import org.pratikpharma.disambiguation.ontology.queries.PrefLabelRetriever;
import org.pratikpharma.disambiguation.ontology.queries.TUIRetriever;
import org.pratikpharma.util.EmptyResultsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparqy.api.queries.QueryProcessor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ClassSemanticInformationImpl implements ClassSemanticInformation {

    private static final Logger logger = LoggerFactory.getLogger(ClassSemanticInformationImpl.class);
    private static final String DISAMBIBUATE_CACHE_PREFIX = "disamb_";
    private final JedisPool jedisPool;

    private final String classURI;
    private String textualSignature;
    private final CUIDefinitions cuiDefinitions;

    private final List<CUI> cuis;

    private final List<String> tuis;

    private final String keyCachePrefix;


    ClassSemanticInformationImpl(final String classURI, final CUIDefinitions cuiDefinitions, final JedisPool jedisPool, final String keyCachePrefix) {
        this.classURI = classURI;
        textualSignature = "";

        this.cuiDefinitions = cuiDefinitions;
        cuis = new ArrayList<>();
        tuis = new ArrayList<>();
        this.jedisPool = jedisPool;
        this.keyCachePrefix = keyCachePrefix;
    }

    @Override
    public synchronized String getTextualSignature() {
        if (textualSignature.isEmpty()) {
            try (final Jedis jedis = jedisPool.getResource()) {
                final String key = keyCachePrefix + "_textual_signature_" + classURI;
                final String cached = jedis.get(key);
                if ((cached == null) || cached.isEmpty()) {
                    final StringBuilder stringBuilder = new StringBuilder();
                    try {
                        final QueryProcessor<String> prefLabelProcessor = new PrefLabelRetriever(classURI);
                        final QueryProcessor<String> altLabelProcessor = new AltLabelRetriever(classURI);
                        prefLabelProcessor.runQuery();
                        final List<String> prefLabels = prefLabelProcessor.processResults();
                        prefLabels.forEach(label -> {
                            stringBuilder.append(label);
                            stringBuilder.append(".");
                        });

                        /*altLabelProcessor.runQuery();
                        final List<String> altLabels = altLabelProcessor.processResults();
                        altLabels.forEach(label -> {
                            stringBuilder.append(label);
                            stringBuilder.append(".");
                        });*/

                    } catch (final IOException e) {
                        logger.error(e.getLocalizedMessage());
                    }

                    textualSignature = stringBuilder.toString();
                    jedis.set(key, textualSignature);
                } else {
                    textualSignature = cached;
                }
            }
        }
        return textualSignature;
    }

    @Override
    public synchronized List<CUI> getCUIs() {
        try (final Jedis jedis = jedisPool.getResource()) {
            final String key = keyCachePrefix + "_" + DISAMBIBUATE_CACHE_PREFIX + classURI + "_" + "cuis";
            if (cuis.isEmpty() && !EmptyResultsCache.isEmpty(key, jedis)) {
                try {
                    List<String> cuiStrings = jedis.lrange(key, 0, -1);
                    if (cuiStrings.isEmpty()) {
                        final QueryProcessor<String> cuiProcessor = new CUIRetriever(classURI);
                        cuiProcessor.runQuery();
                        cuiStrings = cuiProcessor.processResults();
                        cuiStrings.forEach(cui -> {
                            final String definition = cuiDefinitions.getCUIDefinition(cui);
                            cuis.add(new CUIImpl(cui, definition));
                        });
                        if (cuiStrings.isEmpty()) {
                            EmptyResultsCache.markEmpty(key, jedis);
                        }
                    }
                } catch (final IOException e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }
        return new ArrayList<>(cuis);
    }

    @Override
    public String getClassURI() {
        return classURI;
    }

    @Override
    public synchronized List<String> getTUIs() {
        try (final Jedis jedis = jedisPool.getResource()) {
            final String key = keyCachePrefix + "_" + DISAMBIBUATE_CACHE_PREFIX + classURI + "_" + "tuis";
            if (tuis.isEmpty() && !EmptyResultsCache.isEmpty(key, jedis)) {
                try {
                    List<String> tuiStrings = jedis.lrange(key, 0, -1);
                    if (tuiStrings.isEmpty()) {
                        final QueryProcessor<String> tuiProcessor = new TUIRetriever(classURI);
                        tuiProcessor.runQuery();
                        tuiStrings = tuiProcessor.processResults();
                        if (tuiStrings.isEmpty()) {
                            EmptyResultsCache.markEmpty(key, jedis);
                        } else {
                            tuis.addAll(tuiStrings);
                        }
                    }
                } catch (final IOException e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }
        return new ArrayList<>(tuis);
    }
}
