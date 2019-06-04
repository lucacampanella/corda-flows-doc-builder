package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.callgraphflows.graphics.components.GGraphBuilder;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import net.corda.core.flows.StartableByRPC;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Drawer {
    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String DEFAULT_OUT_DIR = "build" + FILE_SEP + "graphs" + FILE_SEP;

    public static void drawAllStartableClasses(AnalyzerWithModel analyzerWithModel, String outPath) {

        System.out.println("All matcher names: " + MatcherHelper.getAllMatcherNames());
        System.out.println("All matchers: " + MatcherHelper.getAllMatchers());

        if(!outPath.endsWith(FILE_SEP)) {
            outPath = outPath + FILE_SEP;
        }
        final List<CtClass> startableByRPCClasses = analyzerWithModel.getClassesByAnnotation(StartableByRPC.class);
        System.out.println("Found these classes annotated with @StartableByRPC: ");
        for (CtClass klass : startableByRPCClasses) {
            System.out.println("**** Analyzing class " + klass.getQualifiedName() + " TEST");
            System.out.println("Here");
            System.out.flush();
            try {
                System.out.println("before drawFromClass call");
                System.out.flush();
                new File(outPath).mkdirs();
                drawFromClass(analyzerWithModel, klass, outPath + klass.getQualifiedName() + ".svg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void drawAllStartableClasses(AnalyzerWithModel analyzerWithModel) {
        drawAllStartableClasses(analyzerWithModel, DEFAULT_OUT_DIR);
    }

    public static void drawFromClass(AnalyzerWithModel analyzerWithModel, CtClass klass, String outPath) throws IOException {
        System.out.println("before AnalysisResult call");
        final AnalysisResult analysisResult = analyzerWithModel.analyzeFlowLogicClass(klass);

        GGraphBuilder graphBuilder = new GGraphBuilder();

        graphBuilder.addSession(analysisResult.getClassName(), analysisResult.getStatements());
        final AnalysisResult initiatedClassResult = analysisResult.getCounterpartyClassResult();
        if(initiatedClassResult != null) {
            graphBuilder.addSession(initiatedClassResult.getClassName(), initiatedClassResult.getStatements());
        }
        graphBuilder.drawToFile(outPath);
    }
}
