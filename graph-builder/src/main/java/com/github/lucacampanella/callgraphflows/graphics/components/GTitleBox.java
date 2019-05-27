package com.github.lucacampanella.callgraphflows.graphics.components;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

/**
 * Title box that is put at the beginning of every session / flow to contain the name of the class
 */
public class GTitleBox extends GBaseTextComponent {

    static final int BORDER_DIM = 8; //dimension of border between text and external line
    protected static final Color BACKGROUND_COLOR = Color.WHITE;

    public GTitleBox(String text) {
        super(text);
    }

    @Override
    public void draw(SVGGraphics2D g2) {
        Dimension dim = getDimensions(g2);
        System.out.println(dim);
        Rectangle rect = new Rectangle(getStartX(), getStartY(), dim.width, dim.height);
        GUtils.fillWithColor(g2, rect, BACKGROUND_COLOR);
        g2.draw(rect);

        AttributedString as1 = new AttributedString(getDisplayText());
        as1.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        g2.drawString(as1.getIterator(), getStartX() + BORDER_DIM,
                getStartY() + BORDER_DIM + g2.getFontMetrics().getAscent());
    }

    @Override
    public int getBorderDim() {
        return BORDER_DIM;
    }
}
