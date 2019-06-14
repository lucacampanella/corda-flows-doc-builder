package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.callgraphflows.asciidoc.AsciiDocBuilder;
import com.github.lucacampanella.callgraphflows.asciidoc.AsciiDocIndexBuilder;
import com.github.lucacampanella.callgraphflows.graphics.components.GGraphBuilder;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.ClassDescriptionContainer;
import net.corda.core.flows.StartableByRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Drawer {

    private Drawer() {
        //private constuctor to hide public one
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Drawer.class);

    public static final String DEFAULT_OUT_DIR = Paths.get("build", "graphs").toString();

    public static void drawAllStartableClasses(AnalyzerWithModel analyzerWithModel, String outPath) throws IOException {
        if(outPath == null) {
            outPath = DEFAULT_OUT_DIR;
        }

        AsciiDocIndexBuilder asciiDocIndexBuilder = new AsciiDocIndexBuilder(analyzerWithModel.getAnalysisName());

        final List<CtClass> startableByRPCClasses = analyzerWithModel.getClassesByAnnotation(StartableByRPC.class);
        LOGGER.info("Found these classes annotated with @StartableByRPC: ");
        new File(outPath).mkdirs();
        for (CtClass klass : startableByRPCClasses) {
            LOGGER.info("**** Analyzing class {} ", klass.getQualifiedName());
            drawFromClass(analyzerWithModel, klass, outPath);
            asciiDocIndexBuilder.addFile(klass.getQualifiedName() + ".adoc");
        }
        asciiDocIndexBuilder.writeToFile(Paths.get(outPath, "index.adoc").toString());
    }

    public static void drawAllStartableClasses(AnalyzerWithModel analyzerWithModel) throws IOException {
        drawAllStartableClasses(analyzerWithModel, DEFAULT_OUT_DIR);
    }

    public static void drawFromClass(AnalyzerWithModel analyzerWithModel, CtClass klass, String outPath) throws IOException {
        final AnalysisResult analysisResult = analyzerWithModel.analyzeFlowLogicClass(klass);

        final ClassDescriptionContainer classDescription = analysisResult.getClassDescription();

        GGraphBuilder graphBuilder = new GGraphBuilder();

        graphBuilder.addSession(classDescription.getSimpleName(), analysisResult.getStatements());
        final AnalysisResult initiatedClassResult = analysisResult.getCounterpartyClassResult();
        if(initiatedClassResult != null) {
            graphBuilder.addSession(initiatedClassResult.getClassDescription().getSimpleName(), initiatedClassResult.getStatements());
        }
        graphBuilder.drawToFile(Paths.get(outPath, classDescription.getFullyQualifiedName() + ".svg").toString());

        AsciiDocBuilder asciiDocBuilder = AsciiDocBuilder.fromAnalysisResult(analysisResult);
        asciiDocBuilder.writeToFile(Paths.get(outPath, classDescription.getFullyQualifiedName() + ".adoc").toString());
    }
}
