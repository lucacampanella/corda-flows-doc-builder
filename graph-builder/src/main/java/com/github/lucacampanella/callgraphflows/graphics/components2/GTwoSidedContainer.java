package com.github.lucacampanella.callgraphflows.graphics.components2;


import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GIndentedComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import org.jfree.graphics2d.svg.SVGGraphics2D;

public abstract class GTwoSidedContainer extends GBaseComponent {

    private static final int SPACE_BETWEEN_FLOWS = 20;

    GSubFlowIndented mainSubFlow = new GSubFlowIndented();
    GSubFlowIndented counterpartySubFlow = null;
    int counterpartyStartY = -1;
    GInstruction initiateFlowInstruction = null;
    GBaseText arrowText = null;

    public GTwoSidedContainer() {
        mainSubFlow.setParent(this);
    }


    @Override
    public void draw(SVGGraphics2D g2, int x, int y) {
        if(getHeight(g2) == -1) { //we didn't compute the internal distribution
            computeHeight(g2);
        }
        mainSubFlow.draw(g2, x, y);
        if(hasCounterpartySubFlow()) {
            int counterpartyStartX = x + mainSubFlow.getWidth(g2) + SPACE_BETWEEN_FLOWS;
            counterpartySubFlow.draw(g2, counterpartyStartX, counterpartyStartY);
//todo arrow between the two

//            int arrowBetweenFlowY = initiateFlowInstruction.getMiddleY(g2);
//            int arrowBetweenFlowStartX = initiateFlowInstruction.getRightBorder(g2);
//            int arrowBetweenFlowFinishX = counterpartyStartX + GSubFlow.WIDTH
//                    - GSubFlow.INDENTATION-1;
//            g2.drawLine(arrowBetweenFlowStartX, arrowBetweenFlowY, arrowBetweenFlowFinishX, arrowBetweenFlowY);
        }
        mainSubFlow.drawBrothersAndLinks(g2);
        if(hasCounterpartySubFlow()) {
            counterpartySubFlow.drawBrothersAndLinks(g2);
        }
    }

    public boolean isTwoSidedComponent() {
        return true;
    }

    @Override
    protected int computeHeight(SVGGraphics2D g2) {
        //compute dimensions with draw logic
        if(!hasCounterpartySubFlow()) {
            final ComponentWithRelativeY blockingComp = mainSubFlow.setUpDimensions(g2, null);
            if(blockingComp != null) {
                throw new IllegalStateException("Cannot have a two sided flow with only one side and " +
                        "that side has direct components that have brothers and links");
            }
            return mainSubFlow.getHeight(g2);
        }
        else {
            final ComponentWithRelativeY initiateFlowInstrWithY =
                    mainSubFlow.setUpDimensionsUntilInitiateFlow(g2, initiateFlowInstruction);
            counterpartyStartY = initiateFlowInstrWithY.getY() + initiateFlowInstruction.getHeight(g2)/2;
            ComponentWithRelativeY blockingLeftWithY = mainSubFlow.setUpDimensions(g2, initiateFlowInstrWithY);
            ComponentWithRelativeY blockingRightWithY = counterpartySubFlow.setUpDimensions(g2, null);

            GBaseSimpleComponent blockingLeft;
            GBaseSimpleComponent blockingRight;
            while(blockingLeftWithY != null && blockingRightWithY != null) {
                blockingLeft = (GBaseSimpleComponent) blockingLeftWithY.getComp();
                blockingRight = (GBaseSimpleComponent) blockingRightWithY.getComp();
                if(blockingLeft.isBrotherWith(blockingRight)) {
                    int maxY = Math.max(blockingLeftWithY.getY(), blockingRightWithY.getY() + counterpartyStartY);
                    blockingLeftWithY.setY(maxY);
                    blockingRightWithY.setY(maxY - counterpartyStartY);
                    blockingLeftWithY = mainSubFlow.setUpDimensions(g2, blockingLeftWithY);
                    blockingRightWithY = counterpartySubFlow.setUpDimensions(g2, blockingRightWithY);
                }
                else {
                    blockingLeftWithY = mainSubFlow.setUpDimensions(g2, blockingLeftWithY);
                }
            }
            //finish in case they where not aligned, strange, probably error
            while(blockingLeftWithY != null) {
                blockingLeftWithY = mainSubFlow.setUpDimensions(g2, blockingLeftWithY);
            }
            while(blockingRightWithY != null) {
                blockingRightWithY = counterpartySubFlow.setUpDimensions(g2, blockingRightWithY);
            }
            return Math.max(mainSubFlow.getHeight(g2), counterpartySubFlow.getHeight(g2) + counterpartyStartY);
        }
    }

    @Override
    protected int computeWidth(SVGGraphics2D g2) {
        int res = mainSubFlow.getWidth(g2);
        if(hasCounterpartySubFlow()) {
            res += SPACE_BETWEEN_FLOWS + counterpartySubFlow.getWidth(g2);
        }
        return res;
    }

    public boolean hasCounterpartySubFlow() {
        return counterpartySubFlow != null;
    }

    public GSubFlowIndented getMainSubFlow() {
        return mainSubFlow;
    }

    public GSubFlowIndented getCounterpartySubFlow() {
        return counterpartySubFlow;
    }

    public void setMainSubFlow(GSubFlowIndented mainSubFlow) {
        this.mainSubFlow = mainSubFlow;
        mainSubFlow.setParent(this);
        recomputeDims();
    }

    public void setCounterpartySubFlow(GSubFlowIndented counterpartySubFlow, GInstruction initiateFlowInstruction) {
        this.counterpartySubFlow = counterpartySubFlow;
        this.initiateFlowInstruction = initiateFlowInstruction;
        this.counterpartySubFlow.setParent(this);
        recomputeDims();
    }

    public void setArrowText(GBaseText arrowText) {
        this.arrowText = arrowText;
    }

    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        mainSubFlow.drawBrothersAndLinks(g2);
        if(hasCounterpartySubFlow()) {
            counterpartySubFlow.drawBrothersAndLinks(g2);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("main sub flow = [");
        sb.append(mainSubFlow.toString());
        sb.append("]\n");
        if(counterpartySubFlow != null) {
            sb.append("counterpartySubFlow = [");
            sb.append(counterpartySubFlow);
            sb.append("]");
        }

        return sb.toString();
    }
}
