package com.github.lucacampanella.callgraphflows.graphics.components2;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.ClassDescriptionContainer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.InitiateFlow;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;

public class GTwoSidedContainerWithTitles extends GTwoSidedContainer {

    private static int SPACE_BETWEEN_TITLE_AND_CONTENT = 15;

    private GBaseText mainSubFlowTitle = null;
    private GBaseText counterpartySubFlowTitle = null;

    public void setMainSubFlow(GBaseText titleComp, GSubFlowIndented mainSubFlow) {
        super.setMainSubFlow(mainSubFlow);
        this.mainSubFlowTitle = titleComp;
    }

    public void setMainSubFlow(String title, GSubFlowIndented mainSubFlow) {
        super.setMainSubFlow(mainSubFlow);
        setMainSubFlow(getTitleBox(title), mainSubFlow);
    }

    public void setCounterpartySubFlow(GBaseText titleComp, GSubFlowIndented counterpartySubFlow, GInstruction initiateFlowInstruction) {
        super.setCounterpartySubFlow(counterpartySubFlow, initiateFlowInstruction);
        this.counterpartySubFlowTitle = titleComp;
    }

    public void setCounterpartySubFlow(String title, GSubFlowIndented counterpartySubFlow, GInstruction initiateFlowInstruction) {
        super.setCounterpartySubFlow(counterpartySubFlow, initiateFlowInstruction);
        setCounterpartySubFlow(getTitleBox(title), counterpartySubFlow, initiateFlowInstruction);
    }

    @Override
    public int computeHeight(SVGGraphics2D g2) {
        return super.computeHeight(g2) + getTitleSectionHeight(g2);
    }

    private GBaseText getTitleBox(String title) {
        GBaseText titleBox = new GBaseText(title);
        titleBox.setDrawBox(true);
        titleBox.setBackgroundColor(Color.LIGHT_GRAY);
        return titleBox;
    }

    private int getTitleSectionHeight(SVGGraphics2D g2) {
        if(!hasCounterpartySubFlow()) {
            return mainSubFlowTitle.getHeight(g2) + SPACE_BETWEEN_TITLE_AND_CONTENT;
        }
        else {
            int mainFlowSpaceNeeded = mainSubFlowTitle.getHeight(g2) + SPACE_BETWEEN_TITLE_AND_CONTENT
                    - mainSubFlow.getRealStart(g2);
            int counterpartyFlowSpaceNeeded = counterpartySubFlowTitle.getHeight(g2) + SPACE_BETWEEN_TITLE_AND_CONTENT
                    - counterpartySubFlow.getRealStart(g2);

            return Math.max(mainFlowSpaceNeeded, counterpartyFlowSpaceNeeded);
        }
    }

    @Override
    public void draw(SVGGraphics2D g2, int x, int y) {
        mainSubFlowTitle.draw(g2, x, y);
        GUtils.drawLineWithOptions(g2, x + GSubFlowIndented.WIDTH/2, y + mainSubFlowTitle.getHeight(g2),
                x + GSubFlowIndented.WIDTH/2,
                y + mainSubFlowTitle.getHeight(g2) + mainSubFlow.getRectStartOffset(g2) + SPACE_BETWEEN_TITLE_AND_CONTENT,
                Color.GRAY, GUtils.DASHED_STROKE);
        if(hasCounterpartySubFlow()) {
            int counterpartyStartX = x + mainSubFlow.getWidth(g2) + SPACE_BETWEEN_FLOWS;
            counterpartySubFlowTitle.draw(g2, counterpartyStartX, y);
            GUtils.drawLineWithOptions(g2, counterpartyStartX + GSubFlowIndented.WIDTH/2,
                    y + counterpartySubFlowTitle.getHeight(g2),
                    counterpartyStartX + GSubFlowIndented.WIDTH/2,
                    y  + counterpartySubFlowTitle.getHeight(g2) + getCounterpartyStartY() +
                            counterpartySubFlow.getRectStartOffset(g2) + SPACE_BETWEEN_TITLE_AND_CONTENT,
                    Color.GRAY, GUtils.DASHED_STROKE);
        }
        super.draw(g2, x, y + getTitleSectionHeight(g2));
        g2.drawLine(x, y + getTitleSectionHeight(g2),
                x + GSubFlowIndented.WIDTH/2,
                y + getTitleSectionHeight(g2));
    }

    public static GTwoSidedContainerWithTitles fromAnalysisResult(AnalysisResult analysisResult) {
        GTwoSidedContainerWithTitles twoSidedContainer = new GTwoSidedContainerWithTitles();

        final ClassDescriptionContainer classDescription = analysisResult.getClassDescription();

        final GSubFlowIndented mainFlow = GSubFlowIndented.fromBranch(analysisResult.getStatements());
        StringBuilder enteringArrowTextSB = new StringBuilder(classDescription.getNameWithParent());
        for(String annotation : classDescription.getAnnotations()) {
            enteringArrowTextSB.append("\n@");
            enteringArrowTextSB.append(annotation);
        }
        mainFlow.setEnteringArrowText(new GBaseText(enteringArrowTextSB.toString()));

        twoSidedContainer.setMainSubFlow(classDescription.getSimpleName(), mainFlow);

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
            twoSidedContainer.setCounterpartySubFlow(initiatedClassResult.getClassDescription().getSimpleName(),
                    counterpartyFlow,
                    (GInstruction) initiateFlow.getGraphElem());
        }

        return twoSidedContainer;
    }
}
