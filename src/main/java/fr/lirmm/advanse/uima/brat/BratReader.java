/*
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.lirmm.advanse.uima.brat;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import fr.lirmm.advanse.uima.brat.internal.model.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.*;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;


/**
 * Reader for the brat format.
 * 
 * @see <a href="http://brat.nlplab.org/standoff.html">brat standoff format</a>
 * @see <a href="http://brat.nlplab.org/configuration.html">brat configuration format</a>
 */
public class BratReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;
    
    /**
     * Types that are relations. It is mandatory to provide the type name followed by two feature
     * names that represent Arg1 and Arg2 separated by colons, e.g. 
     * <code>de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency:Governor:Dependent{A}</code>.
     * Additionally, a subcategorization feature may be specified.
     */
    public static final String PARAM_RELATION_TYPES = "relationTypes";
    @ConfigurationParameter(name = PARAM_RELATION_TYPES, mandatory = true, defaultValue = { 
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency:Governor:Dependent{A}" 
            })
    private Set<String> relationTypes;
    private Map<String, RelationParam> parsedRelationTypes;

    /**
     * Types that are text annotations. It is mandatory to provide the type name which can
     * optionally be followed by a subcategorization feature. Using this parameter is
     * only necessary to specify a subcategorization feature. Otherwise, text annotation types are
     * automatically detected.
     */
    public static final String PARAM_TEXT_ANNOTATION_TYPES = "textAnnotationTypes";
    @ConfigurationParameter(name = PARAM_TEXT_ANNOTATION_TYPES, mandatory = true, defaultValue = {})
    private Set<String> textAnnotationTypes;
    private Map<String, TextAnnotationParam> parsedTextAnnotationTypes;

    public static final String PARAM_TYPE_MAPPINGS = "typeMappings";
    @ConfigurationParameter(name = PARAM_TYPE_MAPPINGS, mandatory = false, defaultValue = {
//            "Token -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
//            "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization",
//            "Location -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location"
            })
    private String[] typeMappings;
    private TypeMapping typeMapping;
    
    private Map<String, AnnotationFS> spanIdMap;
    
    private Set<String> warnings;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        parsedRelationTypes = new HashMap<>();
        for (String rel : relationTypes) {
            RelationParam p = RelationParam.parse(rel);
            parsedRelationTypes.put(p.getType(), p);
        }

        parsedTextAnnotationTypes = new HashMap<>();
        for (String rel : textAnnotationTypes) {
            TextAnnotationParam p = TextAnnotationParam.parse(rel);
            parsedTextAnnotationTypes.put(p.getType(), p);
        }

        typeMapping = new TypeMapping(typeMappings);

        warnings = new LinkedHashSet<String>();
    }
    
    @Override
    public void close()
        throws IOException
    {
        super.close();
        
        for (String warning : warnings) {
            getLogger().warn(warning);
        }
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        spanIdMap = new HashMap<>();
        
        Resource res = nextFile();
        initCas(aJCas, res);

        readText(aJCas, res);
        readAnnotations(aJCas, res);
    }

    private void readAnnotations(JCas aJCas, Resource aRes)
        throws IOException
    {
        BratAnnotationDocument doc;
        try (Reader r = new InputStreamReader(aRes.getInputStream(), encoding)) {
            doc = BratAnnotationDocument.read(r);
        }
        
        CAS cas = aJCas.getCas();
        TypeSystem ts = aJCas.getTypeSystem();
        
        List<BratRelationAnnotation> relations = new ArrayList<>();
        List<BratEventAnnotation> events = new ArrayList<>();
        for (BratAnnotation anno : doc.getAnnotations()) {
            Type type = typeMapping.getUimaType(ts, anno);
            if (anno instanceof BratTextAnnotation) {
                create(cas, type, (BratTextAnnotation) anno);
            }
            else if (anno instanceof BratRelationAnnotation) {
                relations.add((BratRelationAnnotation) anno);
            }
            else if (anno instanceof BratEventAnnotation) {
                create(cas, type, (BratEventAnnotation) anno);
                events.add((BratEventAnnotation) anno);
            }
            else {
                throw new IllegalStateException("BioPortalAnnotation type [" + anno.getClass()
                        + "] is currently not supported.");
            }
        }
        
        // Go through the relations now
        for (BratRelationAnnotation rel : relations) {
            Type type = typeMapping.getUimaType(ts, rel);
            create(cas, type, rel);
        }
        
        // Go through the events again and handle the slots
        for (BratEventAnnotation e : events) {
            Type type = typeMapping.getUimaType(ts, e);
            fillSlots(cas, type, doc, e);
        }
    }

    private void readText(JCas aJCas, Resource res)
        throws IOException
    {
        String annUrl = res.getResource().getURL().toString();
        String textUrl = FilenameUtils.removeExtension(annUrl) + ".txt";

        try (InputStream is = new BufferedInputStream(new URL(textUrl).openStream())) {
            aJCas.setDocumentText(IOUtils.toString(is, encoding));
        }
    }
    
    private void create(CAS aCAS, Type aType, BratTextAnnotation aAnno)
    {
        TextAnnotationParam param = parsedTextAnnotationTypes.get(aType.getName());
        
        AnnotationFS anno = aCAS.createAnnotation(aType, aAnno.getBegin(), aAnno.getEnd());
        
        if (param != null && param.getSubcat() != null) {
            anno.setStringValue(getFeature(anno, param.getSubcat()), aAnno.getType());
        }
        
        fillAttributes(anno, aAnno.getAttributes());
        aCAS.addFsToIndexes(anno);
        spanIdMap.put(aAnno.getId(), anno);
    }

    private void create(CAS aCAS, Type aType, BratEventAnnotation aAnno)
    {
        AnnotationFS anno = aCAS.createAnnotation(aType, 
                aAnno.getTriggerAnnotation().getBegin(), aAnno.getTriggerAnnotation().getEnd());
        fillAttributes(anno, aAnno.getAttributes());
        
        // Slots cannot be handled yet because they might point to events that have not been 
        // created yet.
        
        aCAS.addFsToIndexes(anno);
        spanIdMap.put(aAnno.getId(), anno);
    }
    
    private void create(CAS aCAS, Type aType, BratRelationAnnotation aAnno)
    {
        RelationParam param = parsedRelationTypes.get(aType.getName());
        
        AnnotationFS arg1 = spanIdMap.get(aAnno.getArg1Target());
        AnnotationFS arg2 = spanIdMap.get(aAnno.getArg2Target());
        
        FeatureStructure anno = aCAS.createFS(aType);
        
        anno.setFeatureValue(getFeature(anno, param.getArg1()), arg1);
        anno.setFeatureValue(getFeature(anno, param.getArg2()), arg2);
        
        AnnotationFS anchor = null;
        if (param.getFlags1().contains(RelationParam.FLAG_ANCHOR) && 
                param.getFlags2().contains(RelationParam.FLAG_ANCHOR)) {
            throw new IllegalStateException("Only one argument can be the anchor.");
        }
        else if (param.getFlags1().contains(RelationParam.FLAG_ANCHOR)) {
            anchor = arg1;
        }
        else if (param.getFlags2().contains(RelationParam.FLAG_ANCHOR)) {
            anchor = arg2;
        }
        
        if (param.getSubcat() != null) {
            anno.setStringValue(getFeature(anno, param.getSubcat()), aAnno.getType());
        }
        
        if (anchor != null) {
            anno.setIntValue(anno.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_BEGIN),
                    anchor.getBegin());
            anno.setIntValue(anno.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_END),
                    anchor.getEnd());
        }
        else {
            TypeSystem ts = aCAS.getTypeSystem();
            if (ts.subsumes(ts.getType(CAS.TYPE_NAME_ANNOTATION), anno.getType())) {
                warnings.add("Relation type [" + aType.getName()
                        + "] has offsets but no anchor is specified.");
            }
        }
        
        fillAttributes(anno, aAnno.getAttributes());
        
        aCAS.addFsToIndexes(anno);
    }

    private void fillAttributes(FeatureStructure aAnno, Collection<BratAttribute> aAttributes)
    {
        for (BratAttribute attr : aAttributes) {
            // Try treating the attribute name as an unqualified name, then as a qualified name.
            Feature feat = aAnno.getType().getFeatureByBaseName(attr.getName());
            if (feat == null) {
                String featName = attr.getName().replace('_', ':');
                featName = featName.substring(featName.indexOf(TypeSystem.FEATURE_SEPARATOR) + 1);
                feat = aAnno.getType().getFeatureByBaseName(featName);
            }

            // FIXME HACK! We may not find a "role" feature from slot links in the target type
            // because it should be in the link type. This here is a bad hack, but it should work
            // as long as the target type doesn't define a "role" feature itself.
            if ((("role".equals(attr.getName())) || attr.getName().endsWith("_role"))
                    && feat == null) {
                return;
            }

            if (feat == null) {
                throw new IllegalStateException("Type [" + aAnno.getType().getName()
                        + "] has no feature named [" + attr.getName() + "]");
            }
            
            if (attr.getValues().length == 0) {
                // Nothing to do
            }
            else if (attr.getValues().length == 1) {
                aAnno.setFeatureValueFromString(feat, attr.getValues()[0]);
            }
            else {
                throw new IllegalStateException("Multi-valued attributes currently not supported");
            }
        }
    }
    
    private void fillSlots(CAS aCas, Type aType, BratAnnotationDocument aDoc, BratEventAnnotation aE)
    {
        AnnotationFS event = spanIdMap.get(aE.getId());
        Map<String, List<BratEventArgument>> groupedArgs = aE.getGroupedArguments();
        
        for (Entry<String, List<BratEventArgument>> slot : groupedArgs.entrySet()) {
            // Resolve the target IDs to feature structures
            List<FeatureStructure> targets = new ArrayList<>();
            
            // Lets see if there is a multi-valued feature by the name of the slot
            if (FSUtil.hasFeature(event, slot.getKey())
                    && FSUtil.isMultiValuedFeature(event, slot.getKey())) {
                for (BratEventArgument arg : slot.getValue()) {
                    FeatureStructure target = spanIdMap.get(arg.getTarget());
                    if (target == null) {
                        throw new IllegalStateException("Unable to resolve id [" + arg.getTarget()
                                + "]");
                    }
                    
                    // Handle WebAnno-style slot links
                    // FIXME It would be better if the link type could be configured, e.g. what
                    // is the name of the link feature and what is the name of the role feature...
                    // but right now we just keep it hard-coded to the values that are used
                    // in the DKPro Core SemArgLink and that are also hard-coded in WebAnno
                    Type componentType = event.getType().getFeatureByBaseName(slot.getKey())
                            .getRange().getComponentType();
                    if (CAS.TYPE_NAME_TOP
                            .equals(aCas.getTypeSystem().getParent(componentType).getName())) {
                        BratAnnotation targetAnno = aDoc.getAnnotation(arg.getTarget());
                        BratAttribute roleAttr = targetAnno.getAttribute("role");
                        if (roleAttr == null) {
                            roleAttr = targetAnno.getAttribute(
                                    target.getType().getName().replace('.', '-') + "_role");
                        }
                        FeatureStructure link = aCas.createFS(componentType);
                        FSUtil.setFeature(link, "role", roleAttr.getValues());
                        FSUtil.setFeature(link, "target", target);
                        target = link;
                    }
                    
                    targets.add(target);
                }
                FSUtil.setFeature(event, slot.getKey(), targets);
            }
            // Lets see if there is a single-valued feature by the name of the slot
            else if (FSUtil.hasFeature(event, slot.getKey())) {
                for (BratEventArgument arg : slot.getValue()) {
                    AnnotationFS target = spanIdMap.get(arg.getTarget());
                    if (target == null) {
                        throw new IllegalStateException("Unable to resolve id [" + arg.getTarget()
                                + "]");
                    }
                    
                    String fname = arg.getSlot() + (arg.getIndex() > 0 ? arg.getIndex() : "");
                    if (FSUtil.hasFeature(event, fname)) {
                        FSUtil.setFeature(event, fname, target);
                    }
                    else {
                        throw new IllegalStateException("Type [" + event.getType().getName()
                                + "] has no feature naemd [" + fname + "]");
                    }
                }
            }
            else {
                throw new IllegalStateException("Type [" + event.getType().getName()
                        + "] has no feature naemd [" + slot.getKey() + "]");
            }
        }
    }

    private Feature getFeature(FeatureStructure aFS, String aName)
    {
        Feature f = aFS.getType().getFeatureByBaseName(aName);
        if (f == null) {
            throw new IllegalArgumentException("Type [" + aFS.getType().getName()
                    + "] has no feature called [" + aName + "]");
        }
        return f;
    }    
}
