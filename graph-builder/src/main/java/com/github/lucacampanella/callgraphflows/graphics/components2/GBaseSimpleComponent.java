package com.github.lucacampanella.callgraphflows.graphics.components2;


import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Set;

public abstract class GBaseSimpleComponent extends GBaseComponent {
    protected GBaseSimpleComponent brother = null;
    protected GBaseSimpleComponent enteringBrother = null;
    protected Set<GBaseSimpleComponent> links = new HashSet<>();
    private int lastDrawnStartX = -1;
    private int lastDrawnStartY = -1;

    public void setBrother(GBaseSimpleComponent brother) {
        this.brother = brother;
        brother.setEnteringBrother(this);
    }

    @Override
    public void draw(SVGGraphics2D g2, int x, int y) {
        lastDrawnStartX = x;
        lastDrawnStartY = y;
    }

    public void setEnteringBrother(GBaseSimpleComponent brother) {
        enteringBrother = brother;
    }

    public void addLink(GBaseSimpleComponent link) {
        links.add(link);
    }

    public GBaseSimpleComponent getBrother() {
        return brother;
    }

    public Set<GBaseSimpleComponent> getLinks() {
        return links;
    }

    public boolean hasBrother() {
        return brother != null;
    }

    public boolean hasEnteringBrother() {
        return enteringBrother != null;
    }

    public boolean hasAnyBrother() {
        return hasBrother() || hasEnteringBrother();
    }

    public boolean hasAsBrother(GBaseSimpleComponent brother) {
        return this.brother == brother;
    }

    public boolean isBrotherWith(GBaseSimpleComponent brother) {
        return hasAsBrother(brother) || brother.hasAsBrother(this);
    }

    public boolean isSimpleComponent() {
        return true;
    }

    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        if(brother != null) {
            drawLinkArrowFromFirstToSecond(g2, this, brother);
        }
        links.forEach(link -> drawLinkArrowFromFirstToSecond(g2, this, link));
    }

    private static void drawLinkArrowFromFirstToSecond(SVGGraphics2D g2,
                                                       GBaseSimpleComponent first,
                                                       GBaseSimpleComponent second) {
        int firstMiddleX = first.lastDrawnStartX + (first.getWidth(g2)/2);
        int secondMiddleX = second.lastDrawnStartX + (second.getWidth(g2)/2);
        int firstMiddleY = first.lastDrawnStartY + (first.getHeight(g2)/2);
        int secondMiddleY = second.lastDrawnStartY + (second.getHeight(g2)/2);
        int firstX;
        int secondX;

        if(firstMiddleX < secondMiddleX) {
            firstX = first.lastDrawnStartX + first.getWidth(g2);
            secondX = second.lastDrawnStartX;
        } else {
            firstX = first.lastDrawnStartX;
            secondX = second.lastDrawnStartX + second.getWidth(g2);
        }
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);

        GUtils.drawArrowWithOptions(g2, new Line2D.Double(firstX, firstMiddleY, secondX, secondMiddleY),
                Color.GRAY, dashed);
    }

    public int getLastDrawnStartX() {
        return lastDrawnStartX;
    }

    public int getLastDrawnStartY() {
        return lastDrawnStartY;
    }
}
