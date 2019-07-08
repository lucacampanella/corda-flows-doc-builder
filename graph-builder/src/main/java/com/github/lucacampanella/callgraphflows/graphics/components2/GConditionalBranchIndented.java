package com.github.lucacampanella.callgraphflows.graphics.components2;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;

import java.awt.*;

public class GConditionalBranchIndented extends GBaseIndentedContainer {

    private static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY; //new Color(214, 204, 74); //yellowish

    public GConditionalBranchIndented(GBaseText enteringArrowText) {
        this(enteringArrowText, null);
    }

    public GConditionalBranchIndented(GBaseText enteringArrowText, GBaseText exitingArrowText) {
        super(enteringArrowText, exitingArrowText);
    }

    public GConditionalBranchIndented() {
        this(null, null);
    }

    @Override
    protected Color getAwtColor() {
        final GConditionalBranchIndented containerBranch = getContainerBranch();
        if(containerBranch == null) {
            return BACKGROUND_COLOR;
        }

        return GUtils.makeBrighter(containerBranch.getAwtColor());
    }
}
