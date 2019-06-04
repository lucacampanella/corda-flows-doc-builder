package com.github.lucacampanella.callgraphflows.graphics.components;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class GBaseTextComponent extends GBaseGraphicComponent {

    public static final Color LESS_IMPORTANT_TEXT_COLOR = Color.GRAY;

    static final int BORDER_DIM = 4; //dimension of border between text and external line
    String text;
    Color textColor = Color.BLACK;
    boolean drawBox = false;
    protected Color backgroundColor = Color.WHITE;

    public GBaseTextComponent(String text) {
        this.text = text;
    }

    @Override
    public void draw(SVGGraphics2D g2) {
        Dimension dim = getDimensions(g2);
        Rectangle rect = new Rectangle(getStartX(), getStartY(), dim.width, dim.height);
        GUtils.fillWithColor(g2, rect, getBackgroundColor());
        if(drawBox) {
            g2.draw(rect);
        }

        Color backupColor = g2.getColor();
        g2.setColor(getTextColor());
        g2.drawString(getDisplayText(), getStartX() + getBorderDim(), getStartY() + getBorderDim() + g2.getFontMetrics().getAscent());
        g2.setColor(backupColor);
    }

    @Override
    public Dimension computeDimensions(SVGGraphics2D g2) {

//        TextLayout textLayout = new TextLayout(
//                new AttributedString(text).getIterator(),
//                g2.getFontRenderContext()
//        );
//        Rectangle2D.Float stringBounds = ( Rectangle2D.Float ) textLayout.getBounds();

//        TextLayout layout = new TextLayout(text, g2.getFont(), g2.getFontRenderContext());
//        Rectangle stringBounds = layout.getPixelBounds(null, 0, 0);

        //todo: find better way to compute this, just the length of the string is wrong
        final Rectangle2D stringBounds = g2.getFont().getStringBounds(getDisplayText(), g2.getFontRenderContext());
        Dimension res = new Dimension(
                GUtils.doubleToInt(stringBounds.getWidth()/1.02) + 2*getBorderDim(),
                GUtils.doubleToInt(stringBounds.getHeight()) + 2*getBorderDim());
        //System.out.println(stringBounds + " : " + res);
        return res;
    }

    public int getBorderDim() {
        return BORDER_DIM;
    }

    public String getDisplayText(){
        return getText();
    }

    public String getText(){
        return text;
    }

    public void setText(String text) {
        this.text = text;
        recomputeDimensions = true;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public boolean isDrawBox() {
        return drawBox;
    }

    public void setDrawBox(boolean drawBox) {
        this.drawBox = drawBox;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
