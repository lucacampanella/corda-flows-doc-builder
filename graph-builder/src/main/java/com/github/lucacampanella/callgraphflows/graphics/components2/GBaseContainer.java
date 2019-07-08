package com.github.lucacampanella.callgraphflows.graphics.components2;

import org.jfree.graphics2d.svg.SVGGraphics2D;

public abstract class GBaseContainer extends GBaseComponent {
    public abstract ComponentWithRelativeY setUpDimensions(SVGGraphics2D g2, ComponentWithRelativeY lastCompWithNewY);

    public abstract ComponentWithRelativeY setUpDimensionsUntilInitiateFlow(SVGGraphics2D g2, GBaseText initiateFlowComp);

    public boolean isContainerComponent() {
        return true;
    }
}
