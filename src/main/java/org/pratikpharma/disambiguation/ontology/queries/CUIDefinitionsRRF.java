package org.pratikpharma.disambiguation.ontology.queries;

import org.pratikpharma.disambiguation.ontology.CUIDefinitions;
import org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class CUIDefinitionsRRF implements CUIDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(CUIDefinitionsRRF.class);
    private static final Pattern RRF_FIELD_SEPARATOR_PATTERN = Pattern.compile("|");

    private final Map<String,String> definitions;

    public CUIDefinitionsRRF() {
        definitions = new HashMap<>();
        try {
            try (InputStream inputStream = EHealth2017Task1Annotator.class.getResourceAsStream("/ehealth2017/MRDEF.RRF")) {
                try (final BufferedReader r = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line = r.readLine();
                    while (line != null) {
                        final String[] fields = RRF_FIELD_SEPARATOR_PATTERN.split(line);
                        final String cui = fields[0];
                        final String definition = fields[4];
                        if(definitions.containsKey(cui)){
                            definitions.put(cui, definitions.get(cui) + definition);
                        } else {
                            definitions.put(cui,definition);
                        }

                        line = r.readLine();
                    }
                }
            }
        } catch (final IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    @Override
    public String getCUIDefinition(final String cui) {
        return definitions.get(cui);
    }
}
