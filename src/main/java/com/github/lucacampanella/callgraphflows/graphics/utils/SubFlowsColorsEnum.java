package com.github.lucacampanella.callgraphflows.graphics.utils;

import java.awt.*;

public enum SubFlowsColorsEnum {

    PURPLE, PINK, ORANGE, PINK2;

    private Color awtColor;

    static {
        //REDDISH.awtColor = new Color(255, 91, 165);
        PURPLE.awtColor = new Color(166, 112, 170);
        PINK.awtColor = Color.PINK;
        PINK2.awtColor = PINK.awtColor;
        ORANGE.awtColor = Color.ORANGE;
    }

    public Color getAwtColor() {
        return awtColor;
    }

    private static SubFlowsColorsEnum[] vals = values();
    public SubFlowsColorsEnum next()
    {
        return vals[(this.ordinal()+1) % vals.length];
    }
}
