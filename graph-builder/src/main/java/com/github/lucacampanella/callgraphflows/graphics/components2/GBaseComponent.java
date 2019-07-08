package com.github.lucacampanella.callgraphflows.graphics.components2;

import org.jfree.graphics2d.svg.SVGGraphics2D;

public abstract class GBaseComponent {
    protected int height = -1;
    protected int width = -1;
    GBaseComponent parent;

    public abstract void draw(SVGGraphics2D g2, int startX, int startY);

    public int getHeight(SVGGraphics2D g2) {
        if(height == -1) {
            height = computeHeight(g2);
        }
        return height;
    }
    protected abstract int computeHeight(SVGGraphics2D g2);
    public int getWidth(SVGGraphics2D g2) {
        if(width == -1) {
            width = computeWidth(g2);
        }
        return width;
    }

    protected abstract int computeWidth(SVGGraphics2D g2);

    protected void recomputeHeight() {
        height = -1;
    }

    protected void recomputeWidth() {
        width = -1;
    }

    protected void recomputeDims() {
        recomputeHeight();
        recomputeWidth();
    }

    public GBaseComponent getParent() {
        return parent;
    }

    public void setParent(GBaseComponent parent) {
        this.parent = parent;
    }


    public GSubFlowIndented getContainerFlow() {
        if(getParent() == null) {
            return null;
        }
        if(getParent() instanceof GSubFlowIndented) {
            return (GSubFlowIndented) getParent();
        }
        return getParent().getContainerFlow();
    }

    public GConditionalBranchIndented getContainerBranch() {
        if(getParent() == null || getParent() instanceof GSubFlowIndented) {
            return null;
        }
        if(getParent() instanceof GConditionalBranchIndented) {
            return (GConditionalBranchIndented) getParent();
        }
        return getParent().getContainerBranch();
    }

    public abstract void drawBrothersAndLinks(SVGGraphics2D g2);


    public boolean isContainerComponent() {
        return false;
    }
    public boolean isSimpleComponent() {
        return false;
    }
    public boolean isTwoSidedComponent() {
        return false;
    }

    @Override
    public abstract String toString();
}
