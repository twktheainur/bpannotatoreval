package org.pratikpharma.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@SuppressWarnings("OverlyNestedMethod")
final class QuaeroSubCorpusFilter {
    private static final Logger logger = LoggerFactory.getLogger(QuaeroSubCorpusFilter.class);
    private static final Pattern FILE_EXTENSION_SEPARATOR = Pattern.compile("[.]");

    private QuaeroSubCorpusFilter() {
    }

    public static void main(final String... args) throws IOException {
        final Path corpusPath = Paths.get(args[0]);
        final Path targetCorpusPath = Paths.get(args[0] + "_french_filtered");
        final Path cuiListFile = Paths.get(args[1]);

        final Collection<String> cuiSet = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(cuiListFile)) {
            String line = "";
            while (line != null) {
                line = reader.readLine();
                cuiSet.add(line);
            }
        } catch (final IOException e) {
            logger.error("FATAL ERROR WHEN READING CUI LIST: {}. Aborting.", e.getLocalizedMessage());
            System.exit(1);
        }

        if (Files.exists(targetCorpusPath)) {
            logger.error("The target directory '{}' already exists, please delete it if you want to continue.", targetCorpusPath);
            System.exit(1);
        }
        Files.createDirectory(targetCorpusPath);


        for (final Path annotationFile : Files
                .list(corpusPath)
                .filter(path -> path.getFileName().toString().endsWith("ann"))
                .collect(Collectors.toList())) {
            handleAnnotationFile(annotationFile,cuiSet,targetCorpusPath);

        }

    }

    private static void handleAnnotationFile(final Path annotationFile, final Collection<String> cuiSet, final Path targetCorpusPath) throws IOException {
        final String fileNameRoot = FILE_EXTENSION_SEPARATOR
                .split(annotationFile.getFileName()
                        .toString())[0];
        final Path textFile = Paths.get(targetCorpusPath.toString(),fileNameRoot+".txt");
        final Path targetAnnotationFile = Paths.get(targetCorpusPath.toString(),fileNameRoot+".ann");
        Files.copy(Paths.get(annotationFile.getParent().toString(), fileNameRoot+".txt"),textFile);
        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(targetAnnotationFile))) {
            try(BufferedReader sourceReader = Files.newBufferedReader(annotationFile)){
                String line = "";
                //no inspect all
                while(line!=null){
                    line = sourceReader.readLine();
                    if(line!=null) {
                        final String annotationLine = line;
                        line = sourceReader.readLine();
                        final String noteLine = line;
                        if(line!=null) {
                            final String[] noteFields = noteLine.split("\t");
                            if (noteFields.length < 3) {
                                logger.error("Invalid Format for current line");
                                continue;
                            }
                            final String[] cuis = noteFields[2].split(",");
                            final List<String> cuiList = Arrays.asList(cuis);
                            if (areAllCUIsFoundInReference(cuiSet, cuiList)) {
                                printWriter.println(annotationLine);
                                printWriter.println(noteLine);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean areAllCUIsFoundInReference(final Collection<String> cuiSet, final Iterable<String> cuis){
        boolean found = true;
        final Iterator<String> cuiIterator = cuis.iterator();
        while(found && cuiIterator.hasNext()){
            found = cuiSet.contains(cuiIterator.next());
        }
        return found;
    }
}
