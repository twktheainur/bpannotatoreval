package org.pratikpharma.disambiguation.ontology;

import redis.clients.jedis.JedisPool;

@FunctionalInterface
public interface ClassSemanticInformationFactory {

    ClassSemanticInformationFactory DEFAULT = new DefaultClassSemanticInformationFactory();

    ClassSemanticInformation getSemanticsForClass(final String uri, final JedisPool jedis, final String keyCachePrefix);
}
