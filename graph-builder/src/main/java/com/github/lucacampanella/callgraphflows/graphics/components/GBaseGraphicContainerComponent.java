package com.github.lucacampanella.callgraphflows.graphics.components;

import org.jfree.graphics2d.svg.SVGGraphics2D;

/**
 * The base component to represent any component that is a container of other components
 */
public abstract class GBaseGraphicContainerComponent extends GBaseGraphicComponent{

    private int currY = 0;

    public void resetDrawingInfo() {
        currY = 0;
    }

    public int getCurrY() {
        return currY;
    }

    public void setCurrY(int currY) {
        this.currY = currY;
    }

    public void addToCurrY(int toAdd) {
        setCurrY(getCurrY() + toAdd);
    }

    @Override
    public void setStart(int startX, int startY) {
        super.setStart(startX, startY);
        setCurrY(startY);
    }

    public abstract GBaseGraphicComponent computeDimsStartingFrom(SVGGraphics2D g2, GBaseGraphicComponent comp);

    @Override
    public boolean isContainerComponent() {
        return true;
    }

    @Override
    public int getHeight(SVGGraphics2D g2) {
        return computeHeight(g2);
    }

    @Override
    protected int computeHeight(SVGGraphics2D g2) {
        return getCurrY() - getStartY();
    }
}
