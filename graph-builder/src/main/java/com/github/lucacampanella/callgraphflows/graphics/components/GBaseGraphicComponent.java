package com.github.lucacampanella.callgraphflows.graphics.components;

import com.github.lucacampanella.callgraphflows.graphics.preferences.DefaultPreferences;
import com.github.lucacampanella.callgraphflows.graphics.preferences.PreferencesInterface;
import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The base component to represent anything that can be added to a flow in order
 */
public abstract class GBaseGraphicComponent {

    GBaseGraphicComponent parent;
    boolean recomputeDimensions = true;
    Dimension dimension = null;
    protected Set<GBaseGraphicComponent> brothers = new HashSet<>();
    protected Set<GBaseGraphicComponent> enteringBrothers = new HashSet<>();
    protected Set<GBaseGraphicComponent> links = new HashSet<>();
    private int startX = 0;
    private int currY = 0;
    private int startY = 0;
    private PreferencesInterface pref = DefaultPreferences.getInstance();

    private boolean alreadyReturned = false;

    public boolean isAlreadyReturned() {
        return alreadyReturned;
    }

    public void setAlreadyReturned(boolean alreadyReturned) {
        this.alreadyReturned = alreadyReturned;
    }

    public void resetDrawingInfo() {
        startX = 0;
        startY = 0;
        alreadyReturned = false;
    }

    public int getCurrY() {
        return currY;
    }

    public void setCurrY(int currY) {
        this.currY = currY;
    }

    public abstract void draw(SVGGraphics2D g2);

    public abstract Dimension computeDimensions(SVGGraphics2D g2);

    /**
     * Lazily evaluate dimensions
     * @param g2 the graphics needed to compute the dimensions
     * @return the dimensions of this component
     */
    public Dimension getDimensions(SVGGraphics2D g2) {
        if(recomputeDimensions) {
            dimension = computeDimensions(g2);
            recomputeDimensions = false;
        }
        return dimension;
    }

    public int getMiddleX(SVGGraphics2D g2) {
        return getStartX() + getDimensions(g2).width/2;
    }

    public int getMiddleY(SVGGraphics2D g2) {
        return getStartY() + getDimensions(g2).height/2;
    }

    public int getRightBorder(SVGGraphics2D g2) {
        return getStartX() + getDimensions(g2).width;
    }

    public int getBottomBorder(SVGGraphics2D g2) {
        return getStartY() + getDimensions(g2).height;
    }

    public GBaseGraphicComponent getParent() {
        return parent;
    }

    public void setParent(GBaseGraphicComponent parent) {
        this.parent = parent;
    }

    public GSubFlow getContainerFlow() {
        if(getParent() == null) {
            return null;
        }
        if(getParent() instanceof GSubFlow) {
            return (GSubFlow) getParent();
        }
        return getParent().getContainerFlow();
    }

    public GConditionalBranch getContainerBranch() {
        if(getParent() == null || getParent() instanceof GSubFlow) {
            return null;
        }
        if(getParent() instanceof GConditionalBranch) {
            return (GConditionalBranch) getParent();
        }
        return getParent().getContainerBranch();
    }

    public boolean needsSync() {
        return false;
    }

    public void addBrother(GBaseGraphicComponent brother) {
        brothers.add(brother);
        brother.addEnteringBrother(this);
    }

    public void addEnteringBrother(GBaseGraphicComponent brother) {
        enteringBrothers.add(brother);
    }

    public void addLink(GBaseGraphicComponent link) {
        links.add(link);
    }

    public void setStart(int startX, int startY) {
        setStartX(startX);
        setStartY(startY);
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public void setStartY(int startY) {
        this.startY = startY;
        this.currY = startY;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public Set<GBaseGraphicComponent> getBrothers() {
        return brothers;
    }

    public Set<GBaseGraphicComponent> getLinks() {
        return links;
    }

    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        brothers.forEach(
                brother -> GUtils.drawHorizontalArrowFromFirstToSecond(g2, this, brother)
        );
        links.forEach(
                link -> GUtils.drawHorizontalArrowFromFirstToSecond(g2, this, link)
        );
    }

    public PreferencesInterface getPref() {
        return pref;
    }

    public void setPref(PreferencesInterface pref) {
        this.pref = pref;
    }

    public boolean hasBrothers() {
        return !brothers.isEmpty();
    }

    public boolean hasEnteringBrothers() {
        return !enteringBrothers.isEmpty();
    }

    public boolean hasAnyBrother() {
        return hasBrothers() || hasEnteringBrothers();
    }

    public boolean hasAsBrother(GBaseGraphicComponent brother) {
        return brothers.contains(brother);
    }

    public boolean isBrotherWith(GBaseGraphicComponent brother) {
        return brothers.contains(brother) || brother.hasAsBrother(this);
    }

    public boolean isContainerComponent() {
        return false;
    }

    public abstract GBaseGraphicComponent drawAndConsumeUntilBlocking(SVGGraphics2D g2);
}
