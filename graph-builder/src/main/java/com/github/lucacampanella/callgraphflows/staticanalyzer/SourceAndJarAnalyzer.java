package com.github.lucacampanella.callgraphflows.staticanalyzer;

import net.corda.core.flows.StartableByRPC;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.SpoonException;
import spoon.decompiler.Decompiler;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SourceAndJarAnalyzer extends AnalyzerWithModel {

    private boolean analyzeOnlySources = false;
    private Set<String> srcClassNamesSet;

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceAndJarAnalyzer.class);

    public SourceAndJarAnalyzer(List<String> pathsToFoldersOrSrc) throws IOException {
        init(pathsToFoldersOrSrc, null, null, false);
    }

    public SourceAndJarAnalyzer(String[] unsortedTypesFiles, DecompilerEnum decompilerEnum,
                                boolean analyzeOnlySources) throws IOException {
        List<String> jarPaths = new ArrayList<>();
        List<String> otherPaths = new ArrayList<>();
        for(String path : unsortedTypesFiles) {
            if(path.endsWith(".jar")) {
                jarPaths.add(path);
            }
            else {
                otherPaths.add(path);
            }
        }
        init(otherPaths, jarPaths, decompilerEnum, analyzeOnlySources);
    }

    public SourceAndJarAnalyzer(List<String> pathsToFoldersOrSrc, List<String> pathsToJars,
                                DecompilerEnum decompilerEnum, boolean analyzeOnlySources) throws IOException {
        init(pathsToFoldersOrSrc, pathsToJars, decompilerEnum, analyzeOnlySources);
    }

    private void init(List<String> pathsToFoldersOrSrc, List<String> pathsToJars, DecompilerEnum decompilerEnum,
                      boolean analyzeOnlySources) throws IOException {

        this.analyzeOnlySources = analyzeOnlySources;

        analysisName = pathsToFoldersOrSrc.stream().map(
                pathToJar -> pathToJar.substring(pathToJar.lastIndexOf(System.getProperty("file.separator"))+1)).
                collect(Collectors.joining(", "));

        Set<String> addedClassesNamesSet = new HashSet<>();

        Launcher spoon = new Launcher();

        if(pathsToFoldersOrSrc != null) {
            for (String path : pathsToFoldersOrSrc) {
                final File folderOrSrc = new File(path);
                if (!folderOrSrc.exists()) {
                    throw new RuntimeException("File or folder " + folderOrSrc.getPath() + " does not exist");
                }
                if (folderOrSrc.isDirectory()) {
                    addFolderToModel(addedClassesNamesSet, spoon, folderOrSrc);
                } else {
                    addSingleFileToModel(addedClassesNamesSet, spoon, folderOrSrc);
                }
            }
        }

        if(analyzeOnlySources) {
            srcClassNamesSet = new HashSet<>(addedClassesNamesSet); //we save which classes derive from sources and
            //not decompilation
        }

        if(pathsToJars != null) {
            String decompiledSrcPath =
                    Paths.get(System.getProperty("java.io.tmpdir"), "spoon-camp-tmp", "decompiledSrc").toString();
            final File decompiledSrcFolder = new File(decompiledSrcPath);
            FileUtils.deleteDirectory(decompiledSrcFolder);
            for(String path : pathsToJars) {
                decompileJarToFolder(path, decompiledSrcPath, decompilerEnum);
            }
            addFolderToModel(addedClassesNamesSet, spoon, decompiledSrcFolder);
        }
        spoon.buildModel();
        model = spoon.getModel();
    }

    private static void addFolderToModel(Set<String> addedClassesNamesSet, Launcher spoon, File folder) throws IOException {
        final Collection<File> sourceFiles =
                FileUtils.listFiles(folder, new String[]{"java"}, true);
        for (File srcFile : sourceFiles) {
            addSingleFileToModel(addedClassesNamesSet, spoon, srcFile);
        }
    }

    private static void addSingleFileToModel(Set<String> addedClassesNamesSet, Launcher spoon, File srcFile) throws IOException {
        final String qualifiedName = findQualifiedName(srcFile);
        if(!addedClassesNamesSet.contains(qualifiedName)) {
            addedClassesNamesSet.add(qualifiedName);
            spoon.addInputResource(srcFile.getAbsolutePath());
        }
        else {
            LOGGER.trace("File {} represents class {}, which was already added to the model, skipping", srcFile, qualifiedName);
        }
    }

    public static void decompileJarToFolder(String jarPath,
                                            String ouputDir,
                                            DecompilerEnum decompilerEnum) {
        File decompiledDirectory = new File(ouputDir);
        if (decompiledDirectory.exists() && !decompiledDirectory.canWrite()) {
            throw new SpoonException("Dir " + decompiledDirectory.getPath() + " already exists and is not deletable.");
        }

        if (!decompiledDirectory.exists()) {
            decompiledDirectory.mkdirs();
        }

        Decompiler decompiler = decompilerEnum.getDecompiler(decompiledDirectory);

        File jar = new File(jarPath);
        if (jar.exists() && jar.isFile()) {
            decompiler.decompile(jar.getAbsolutePath());
        } else {
            throw new SpoonException("Jar " + jar.getPath() + " not found.");
        }
    }

    public static String findQualifiedName(File srcFile) throws IOException {
        String res = "";
        try (LineIterator lineIt = FileUtils.lineIterator(srcFile)) {
            while (lineIt.hasNext()) {
                String line = lineIt.nextLine();
                if(line.contains("package ")) {
                    res = line.substring(
                            line.indexOf("package ") + "package ".length(),
                            line.indexOf(';'));
                    break;
                }
            }
        }
        final String path = srcFile.getPath();
        res += "." + path
                .substring(path
                        .lastIndexOf(System.getProperty("file.separator"))+1,
                        path.indexOf(".java"));

        return res;
    }

    @Override
    public List<CtClass> getClassesToBeAnalyzed() {
        if(!analyzeOnlySources) {
            return super.getClassesToBeAnalyzed();
        }
        return super.getClassesToBeAnalyzed().stream().filter(klass ->
            srcClassNamesSet.contains(klass.getTopLevelType().getQualifiedName())).collect(Collectors.toList());
    }

}
