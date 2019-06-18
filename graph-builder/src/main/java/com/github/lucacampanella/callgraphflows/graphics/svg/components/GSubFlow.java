package com.github.lucacampanella.callgraphflows.graphics.svg.components;

import com.github.lucacampanella.callgraphflows.graphics.svg.utils.SubFlowColor;
import com.github.lucacampanella.callgraphflows.graphics.svg.utils.SubFlowsColorsEnum;

import java.awt.*;

public class GSubFlow extends GIndentedComponent {

    public GSubFlow(GBaseTextComponent enteringArrowText) {
        this(enteringArrowText, null);
    }

    public GSubFlow(GBaseTextComponent enteringArrowText, GBaseTextComponent exitingArrowText) {
        super(enteringArrowText, exitingArrowText);
    }

    public GSubFlow() {
        this(null, null);
    }

    @Override
    public void addComponent(GBaseGraphicComponent component) {
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
        final GSubFlow containerFlow = getContainerFlow();
        return containerFlow == null ? new SubFlowColor(SubFlowsColorsEnum.values()[0], 0) :
                    containerFlow.getColor().getNextZeroBrightness();
    }
}
