package com.github.lucacampanella.callgraphflows.graphics.puml;

import com.github.lucacampanella.callgraphflows.graphics.svg.components.GBaseGraphicComponent;
import com.github.lucacampanella.callgraphflows.graphics.svg.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.svg.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.LinkedList;
import java.util.List;

public abstract class PIndentedComponent extends PBaseComponent {
    protected List<PBaseComponent> components = new LinkedList<>();
    protected String enteringArrowText;
    protected String exitingArrowText;

    public PIndentedComponent(String enteringArrowText) {
        this(enteringArrowText, null);
    }

    public PIndentedComponent(String enteringArrowText, String exitingArrowText) {
        this.enteringArrowText = enteringArrowText;
        this.exitingArrowText = exitingArrowText;
    }

    public PIndentedComponent() {
        this(null, null);
    }

    public void addComponent(PBaseComponent component) {
        if(component != null) {
            components.add(component);
            component.setParent(this);
        }
    }

    protected abstract Color getAwtColor();

    public void setEnteringArrowText(String enteringArrowText) {
        this.enteringArrowText = enteringArrowText;
    }

    public void setExitingArrowText(String exitingArrowText) {
        this.exitingArrowText = exitingArrowText;
    }

    public String getEnteringArrowText() {
        return enteringArrowText;
    }

    public String getExitingArrowText() {
        return exitingArrowText;
    }
}
