package com.github.lucacampanella.callgraphflows.graphics.components;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;

public class GConditionalBranch extends GIndentedComponent {

    private static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY; //new Color(214, 204, 74); //yellowish

    public GConditionalBranch(GBaseTextComponent enteringArrowText) {
        this(enteringArrowText, null);
    }

    public GConditionalBranch(GBaseTextComponent enteringArrowText, GBaseTextComponent exitingArrowText) {
        super(enteringArrowText, exitingArrowText);
    }

    public GConditionalBranch() {
        this(null, null);
    }

    @Override
    protected Color getAwtColor() {
        final GConditionalBranch containerBranch = getContainerBranch();
        if(containerBranch == null) {
            return BACKGROUND_COLOR;
        }

        return GUtils.makeBrighter(getPref(), containerBranch.getAwtColor());
    }

    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        super.drawBrothersAndLinks(g2);
        if(enteringArrowText != null) {
            enteringArrowText.drawBrothersAndLinks(g2);
        }
        components.forEach(comp -> comp.drawBrothersAndLinks(g2));
        if(exitingArrowText != null) {
            exitingArrowText.drawBrothersAndLinks(g2);
        }
    }

    @Override
    public void setEnteringArrowText(GBaseTextComponent enteringArrowText) {
        this.enteringArrowText = enteringArrowText;
    }

    @Override
    public void setExitingArrowText(GBaseTextComponent exitingArrowText) {
        this.exitingArrowText = exitingArrowText;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(enteringArrowText != null) {
            sb.append(enteringArrowText.toString());
            sb.append("\n");
        }
        components.forEach(comp -> {
            sb.append("     ");
            sb.append(comp.toString());
            sb.append("\n");
        });
        if(exitingArrowText != null) {
            sb.append(exitingArrowText.toString());
        }

        return sb.toString();
    }

    @Override
    public GBaseTextComponent getEnteringArrowText() {
        return enteringArrowText;
    }

    @Override
    public GBaseTextComponent getExitingArrowText() {
        return exitingArrowText;
    }
}
