package com.github.lucacampanella.callgraphflows.graphics.puml;

import com.github.lucacampanella.callgraphflows.graphics.svg.components.GConditionalBranch;
import com.github.lucacampanella.callgraphflows.graphics.svg.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.graphics.svg.preferences.DefaultPreferences;
import com.github.lucacampanella.callgraphflows.graphics.svg.preferences.PreferencesInterface;
import com.github.lucacampanella.callgraphflows.graphics.svg.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The base component to represent anything that can be added to a flow in order
 */
public abstract class PBaseComponent {

    PBaseComponent parent;
    protected Set<PBaseComponent> brothers = new HashSet<>();
    protected Set<PBaseComponent> links = new HashSet<>();

    private PreferencesInterface pref = DefaultPreferences.getInstance();


    public PBaseComponent getParent() {
        return parent;
    }

    public void setParent(PBaseComponent parent) {
        this.parent = parent;
    }

    public PSubFlow getContainerFlow() {
        if(getParent() == null) {
            return null;
        }
        if(getParent() instanceof PSubFlow) {
            return (PSubFlow) getParent();
        }
        return getParent().getContainerFlow();
    }

    public PConditionalBranch getContainerBranch() {
        if(getParent() == null || getParent() instanceof PSubFlow) {
            return null;
        }
        if(getParent() instanceof PConditionalBranch) {
            return (PConditionalBranch) getParent();
        }
        return getParent().getContainerBranch();
    }


    public void addBrother(PBaseComponent brother) {
        brothers.add(brother);
    }

    public void addLink(PBaseComponent link) {
        links.add(link);
    }

    public Set<PBaseComponent> getBrothers() {
        return brothers;
    }

    public Set<PBaseComponent> getLinks() {
        return links;
    }

    public abstract String getPUMLString();

    @Override
    public String toString() {
        return getPUMLString();
    }

    public PreferencesInterface getPref() {
        return pref;
    }
}
