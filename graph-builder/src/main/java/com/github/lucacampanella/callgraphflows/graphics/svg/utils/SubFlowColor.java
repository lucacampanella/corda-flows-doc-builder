package com.github.lucacampanella.callgraphflows.graphics.svg.utils;

import java.awt.*;

public class SubFlowColor {
    private SubFlowsColorsEnum color;
    private int brightness = 0;

    public SubFlowColor(SubFlowsColorsEnum color, int brightness) {
        this.color = color;
        this.brightness = brightness;
    }

    public SubFlowColor getNextZeroBrightness() {
        return new SubFlowColor(color.next(), 0);
    }

    public SubFlowColor getSameBrightnessPlusOne() {
        return new SubFlowColor(color, brightness+1);
    }

    public Color getAwtColor() {
        Color res = color.getAwtColor();
        for(int i = 0; i < brightness; ++i) {
            res = res.brighter();
        }
        return res;
    }
}
