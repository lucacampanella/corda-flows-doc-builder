package com.github.lucacampanella.callgraphflows.graphics.puml;

import com.github.lucacampanella.callgraphflows.graphics.svg.components.GBaseGraphicComponent;
import com.github.lucacampanella.callgraphflows.graphics.svg.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.svg.components.GIndentedComponent;
import com.github.lucacampanella.callgraphflows.graphics.svg.utils.SubFlowColor;
import com.github.lucacampanella.callgraphflows.graphics.svg.utils.SubFlowsColorsEnum;

import java.awt.*;

public class PSubFlow extends PIndentedComponent {

    public PSubFlow(String enteringArrowText) {
        this(enteringArrowText, null);
    }

    public PSubFlow(String enteringArrowText, String exitingArrowText) {
        super(enteringArrowText, exitingArrowText);
    }

    public PSubFlow() {
        this(null, null);
    }

    @Override
    public void addComponent(PBaseComponent component) {
        if(component != null) {
            components.add(component);
            component.setParent(this);
        }
    }
    @Override
    protected Color getAwtColor() {
        return getColor().getAwtColor();
    }

    protected SubFlowColor getColor() {
        final PSubFlow containerFlow = getContainerFlow();
        return containerFlow == null ? new SubFlowColor(SubFlowsColorsEnum.values()[0], 0) :
                    containerFlow.getColor().getNextZeroBrightness();
    }

    @Override
    public String getPUMLString() {
        return null;
    }
}
