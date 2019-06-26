package com.github.lucacampanella.callgraphflows.graphics.components;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;

public class GSubFlowWithCounterparty extends GBaseGraphicComponent {

    private static final int SPACE_BETWEEN_FLOWS = 20;

    GSubFlow mainSubFlow = new GSubFlow();
    GSubFlow counterpartySubFlow = null;
    GInstruction initiateFlowInstruction = null;
    GBaseTextComponent arrowText = null;

    public GSubFlowWithCounterparty() {
        mainSubFlow.setParent(this);
    }

    @Override
    public void draw(SVGGraphics2D g2) {
        mainSubFlow.setStart(getStartX(), getStartY());
        mainSubFlow.draw(g2);
        if(hasCounterpartySubFlow()) {
            int counterpartyStartX = getStartX() + mainSubFlow.getDimensions(g2).width + SPACE_BETWEEN_FLOWS;
            int counterpartyStartY = initiateFlowInstruction.getMiddleY(g2);

            counterpartySubFlow.setStart(counterpartyStartX, counterpartyStartY);
            counterpartySubFlow.draw(g2);
            int arrowBetweenFlowY = initiateFlowInstruction.getMiddleY(g2);
            int arrowBetweenFlowStartX = initiateFlowInstruction.getRightBorder(g2);
            int arrowBetweenFlowFinishX = counterpartyStartX + GSubFlow.WIDTH
                    - GSubFlow.INDENTATION-1;
            g2.drawLine(arrowBetweenFlowStartX, arrowBetweenFlowY, arrowBetweenFlowFinishX, arrowBetweenFlowY);
        }
    }

    @Override
    public Dimension computeDimensions(SVGGraphics2D g2) {
        Dimension dim = new Dimension();
        dim.height = mainSubFlow.getDimensions(g2).height;
        if(hasCounterpartySubFlow()) {
            //this is a bit of an hack, we draw it somewhere else, just to understand where the
            //initiating statement would be placed

            int startXBackup = mainSubFlow.getStartX();
            int startYBackup = mainSubFlow.getStartY();

            int startXClip = g2.getWidth()+1;
            int startYClip = g2.getHeight()+1;

            mainSubFlow.setStart(startXClip, startYClip);
            mainSubFlow.draw(g2);
            g2.clearRect(startXClip, startYClip,
                    mainSubFlow.getRightBorder(g2), mainSubFlow.getBottomBorder(g2));
            mainSubFlow.setStart(startXBackup, startYBackup);

            int counterpartyRelativeHeight = initiateFlowInstruction.getMiddleY(g2) - startYClip;

            dim.height = Math.max(dim.height,
                    counterpartyRelativeHeight + counterpartySubFlow.getDimensions(g2).height);
        }
        dim.width = mainSubFlow.getDimensions(g2).width;
        if(hasCounterpartySubFlow()) {
            dim.width += SPACE_BETWEEN_FLOWS + counterpartySubFlow.getDimensions(g2).width;
        }
        return dim;
    }


    public boolean hasCounterpartySubFlow() {
        return counterpartySubFlow != null;
    }

    public GSubFlow getMainSubFlow() {
        return mainSubFlow;
    }

    public GSubFlow getCounterpartySubFlow() {
        return counterpartySubFlow;
    }

    public void setMainSubFlow(GSubFlow mainSubFlow) {
        this.mainSubFlow = mainSubFlow;
        mainSubFlow.setParent(this);
        recomputeDimensions = true;
    }

    public void setCounterpartySubFlow(GSubFlow counterpartySubFlow, GInstruction initiateFlowInstruction) {
        this.counterpartySubFlow = counterpartySubFlow;
        this.initiateFlowInstruction = initiateFlowInstruction;
        recomputeDimensions = true;
        this.counterpartySubFlow.setParent(this);
    }

    public void setArrowText(GBaseTextComponent arrowText) {
        this.arrowText = arrowText;
    }
}
