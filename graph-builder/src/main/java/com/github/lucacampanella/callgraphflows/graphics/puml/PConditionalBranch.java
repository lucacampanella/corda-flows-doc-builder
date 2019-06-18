package com.github.lucacampanella.callgraphflows.graphics.puml;

import com.github.lucacampanella.callgraphflows.graphics.svg.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.svg.components.GIndentedComponent;
import com.github.lucacampanella.callgraphflows.graphics.svg.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;

public class PConditionalBranch extends PIndentedComponent {

    private static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY; //new Color(214, 204, 74); //yellowish

    public PConditionalBranch(String enteringArrowText) {
        this(enteringArrowText, null);
    }

    public PConditionalBranch(String enteringArrowText, String exitingArrowText) {
        super(enteringArrowText, exitingArrowText);
    }

    public PConditionalBranch() {
        this(null, null);
    }

    @Override
    protected Color getAwtColor() {
        final PConditionalBranch containerBranch = getContainerBranch();
        if(containerBranch == null) {
            return BACKGROUND_COLOR;
        }

        return GUtils.makeBrighter(getPref(), containerBranch.getAwtColor());
    }

    @Override
    public String getPUMLString() {
        return null;
    }
}
