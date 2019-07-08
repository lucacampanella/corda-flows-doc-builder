package com.github.lucacampanella.callgraphflows.graphics.components2;

import org.jfree.graphics2d.svg.SVGGraphics2D;

public class ComponentWithRelativeY {
    private GBaseComponent comp = null;
    private int y = -1;

    public ComponentWithRelativeY() {
    }

    public ComponentWithRelativeY(GBaseComponent comp, int y) {
        this.comp = comp;
        this.y = y;
    }

    public ComponentWithRelativeY(GBaseComponent comp) {
        this.comp = comp;
    }

    public GBaseComponent getComp() {
        return comp;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void drawRelative(SVGGraphics2D g2, int startContX, int startContY) {
        getComp().draw(g2, startContX, startContY + y);
    }

    public ComponentWithRelativeY subtractingToY(int startingOuterObjY) {
        y -= startingOuterObjY;
        return this;
    }

    public ComponentWithRelativeY addingToY(int startingOuterObjY) {
        y += startingOuterObjY;
        return this;
    }

    @Override
    public String toString() {
        return "[" + getY() +"] " + (getComp() == null ? " NULL COMP " : getComp().toString());
    }
}
