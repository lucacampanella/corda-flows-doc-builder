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
import java.util.List;

public class Drawer {

    private Drawer() {
        //private constuctor to hide public one
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Drawer.class);

    private static final String FILE_SEP = System.getProperty("file.separator");
    public static final String DEFAULT_OUT_DIR = "build" + FILE_SEP + "graphs" + FILE_SEP;

    public static void drawAllStartableClasses(AnalyzerWithModel analyzerWithModel, String outPath) throws IOException {
        if(outPath == null) {
            outPath = DEFAULT_OUT_DIR;
        }
        else if(!outPath.endsWith(FILE_SEP)) {
            outPath = outPath + FILE_SEP;
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
        asciiDocIndexBuilder.writeToFile(outPath + "index.adoc");
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
        graphBuilder.drawToFile(outPath + classDescription.getFullyQualifiedName() + ".svg");

        AsciiDocBuilder asciiDocBuilder = AsciiDocBuilder.fromAnalysisResult(analysisResult);
        asciiDocBuilder.writeToFile(outPath + classDescription.getFullyQualifiedName() + ".adoc");
    }
}
