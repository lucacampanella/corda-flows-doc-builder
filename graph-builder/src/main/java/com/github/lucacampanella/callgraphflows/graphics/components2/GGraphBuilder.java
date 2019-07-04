package com.github.lucacampanella.callgraphflows.graphics.components2;

import com.github.lucacampanella.callgraphflows.graphics.components.*;
import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.ClassDescriptionContainer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.InitiateFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class GGraphBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GGraphBuilder.class);

    private static final int BORDER = 10;
    private static final int MIN_DISTANCE_TITLE_SESSION = 15;

    GTwoSidedContainerWithTitles gTwoSidedContainerWithTitles = new GTwoSidedContainerWithTitles();


    public GGraphBuilder() {
        //empty constructor
    }

    public static GGraphBuilder fromAnalysisResult(AnalysisResult analysisResult) {
        GGraphBuilder graphBuilder = new GGraphBuilder();
        final ClassDescriptionContainer classDescription = analysisResult.getClassDescription();

        final GSubFlowIndented mainFlow = GSubFlowIndented.fromBranch(analysisResult.getStatements());

        StringBuilder enteringArrowTextSB = new StringBuilder(classDescription.getNameWithParent());
        for(String annotation : classDescription.getAnnotations()) {
            enteringArrowTextSB.append("\n@");
            enteringArrowTextSB.append(annotation);
        }

        mainFlow.setEnteringArrowText(new GBaseText(enteringArrowTextSB.toString()));
        graphBuilder.setLeftSession(classDescription.getSimpleName(), mainFlow);

        final AnalysisResult initiatedClassResult = analysisResult.getCounterpartyClassResult();
        if(initiatedClassResult != null) {
            final InitiateFlow initiateFlow = analysisResult.getStatements().getInitiateFlowStatementAtThisLevel()
                    .orElseThrow(() -> new IllegalStateException("Analysis with initiated counterparty, " +
                            "but no initiate Flow instruction at this level"));

            final GSubFlowIndented counterpartyFlow = GSubFlowIndented.fromBranch(initiatedClassResult.getStatements());

            enteringArrowTextSB = new StringBuilder(initiatedClassResult.getClassDescription().getNameWithParent());
            enteringArrowTextSB.append("\n@InitiatedBy(");
            enteringArrowTextSB.append(classDescription.getNameWithParent());
            enteringArrowTextSB.append(")");

            counterpartyFlow.setEnteringArrowText(new GBaseText(enteringArrowTextSB.toString()));
            graphBuilder.setRightSession(initiatedClassResult.getClassDescription().getSimpleName(),
                    counterpartyFlow,
                    (GInstruction) initiateFlow.getGraphElem());
        }

        return graphBuilder;
    }

    public void setLeftSession(String title, GSubFlowIndented flow) {
        gTwoSidedContainerWithTitles.setMainSubFlow(flow);
    }

    public void setRightSession(String title, GSubFlowIndented flow, GInstruction initiatingInstructionOfMainFlow) {
        gTwoSidedContainerWithTitles.setCounterpartySubFlow(flow, initiatingInstructionOfMainFlow);
    }

    public void drawToFile(String path) throws IOException {

        SVGGraphics2D g2 = new SVGGraphics2D(10000, 10000);
        int width = gTwoSidedContainerWithTitles.getWidth(g2);
        int height = gTwoSidedContainerWithTitles.getHeight(g2);

        LOGGER.info("{}", gTwoSidedContainerWithTitles);



        g2 = new SVGGraphics2D(width, height);
        GUtils.fillWithColor(g2, new Rectangle(0,0,width, height), Color.WHITE);

        gTwoSidedContainerWithTitles.draw(g2, 0,0);

        String svgElement = g2.getSVGElement();

        Files.write(Paths.get(path), svgElement.getBytes());
    }
}
