package com.github.lucacampanella.callgraphflows.graphics.components2;

import com.github.lucacampanella.callgraphflows.graphics.utils.SubFlowColor;
import com.github.lucacampanella.callgraphflows.graphics.utils.SubFlowsColorsEnum;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;

public class GSubFlowIndented extends GBaseIndentedContainer {

    public GSubFlowIndented() {

    }

    public GSubFlowIndented(GBaseText enteringArrowText, GBaseText exitingArrowText) {
        super(enteringArrowText, exitingArrowText);
    }

    @Override
    protected Color getAwtColor() {
        return getColor().getAwtColor();
    }

    protected SubFlowColor getColor() {
        final GSubFlowIndented containerFlow = getContainerFlow();
        return containerFlow == null ? new SubFlowColor(SubFlowsColorsEnum.values()[0], 0) :
                containerFlow.getColor().getNextZeroBrightness();
    }

    public static GSubFlowIndented fromBranch(Branch branch) {
        GSubFlowIndented flow = new GSubFlowIndented();

        for(StatementInterface instr : branch.getStatements()) {
            if(instr.getGraphElem() != null) {
                flow.addComponent(instr.getGraphElem());
            }
        }
        return flow;
    }
}
