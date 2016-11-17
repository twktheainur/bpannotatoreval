package org.pratikpharma.umls.groups;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class UMLSSemanticGroupsLoader {
    private final static Logger logger = LoggerFactory.getLogger(UMLSSemanticGroupsLoader.class);

    private UMLSSemanticGroupsLoader() {
    }

    public static Map<String, UMLSGroup> load() {
        return load(UMLSSemanticGroupsLoader.class.getResourceAsStream("/semgroups.ssv"));
    }

    public static Map<String, UMLSGroup> load(InputStream stream) {
        if (stream == null) {
            logger.error("Invalid stream");
            return Collections.emptyMap();
        } else {
            Map<String, UMLSGroup> groups = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                reader.lines().forEach(line -> {
                    String[] fields = line.split(" ");
                    String name = fields[0];
                    String typeString = fields[1];
                    String[] types = typeString.split(",");
                    UMLSGroup group = new UMLSGroup(name);
                    for (String type : types) {
                        group.addType(type);
                    }
                    groups.put(name, group);
                });
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
            }
            return groups;
        }
    }
}
