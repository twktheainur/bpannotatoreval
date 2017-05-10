package org.pratikpharma.disambiguation.ontology;

import org.pratikpharma.disambiguation.ontology.queries.CUIDefinitionsRRF;
import redis.clients.jedis.JedisPool;

public class DefaultClassSemanticInformationFactory implements ClassSemanticInformationFactory {



    private final CUIDefinitions cuiDefinitions;

    DefaultClassSemanticInformationFactory() {
        cuiDefinitions = new CUIDefinitionsRRF();
    }

    @Override
    public ClassSemanticInformation getSemanticsForClass(final String uri, final JedisPool jedisPool, final String keyCachePrefix) {
        return new ClassSemanticInformationImpl(uri,cuiDefinitions,jedisPool,keyCachePrefix);
    }
}
