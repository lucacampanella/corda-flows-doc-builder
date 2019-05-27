package com.github.lucacampanella.callgraphflows.graphics.components;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class GNote extends GBaseTextComponent {

    private static final int FOLD_DIM = 6;

    public GNote(String text) {
        super(text);
    }

    @Override
    public void draw(SVGGraphics2D g2) {
        Dimension dim = getDimensions(g2);

        int foldingPointTopX = getStartX() + dim.width - FOLD_DIM;
        int foldingPointTopY = getStartY();

        int foldingPointRightX = getStartX() + dim.width;
        int foldingPointRightY = getStartY() + FOLD_DIM;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(getStartX(), getStartY());
        path.lineTo(foldingPointTopX, foldingPointTopY);
        path.lineTo(foldingPointRightX, foldingPointRightY);
        path.lineTo(getStartX() + dim.width, getStartY() + dim.height);
        path.lineTo(getStartX(), getStartY() + dim.height);
        path.closePath();

        GUtils.fillWithColor(g2, path, Color.WHITE);


        g2.draw(new Line2D.Double(getStartX(), getStartY(), foldingPointTopX, foldingPointTopY));
        g2.draw(new Line2D.Double(getStartX(), getStartY(), getStartX(), getStartY() + dim.height));
        g2.draw(new Line2D.Double(getStartX(), getStartY() + dim.height,
                getStartX() + dim.width, getStartY() + dim.height));
        g2.draw(new Line2D.Double(getStartX() + dim.width, getStartY() + dim.height,
                foldingPointRightX, foldingPointRightY));

        g2.draw(new Line2D.Double(foldingPointTopX, foldingPointTopY,
                foldingPointTopX, foldingPointTopY + FOLD_DIM));
        g2.draw(new Line2D.Double(foldingPointRightX, foldingPointRightY,
                foldingPointRightX - FOLD_DIM, foldingPointRightY));
        g2.draw(new Line2D.Double(foldingPointRightX, foldingPointRightY,
                foldingPointTopX, foldingPointTopY));

        g2.drawString(text, getStartX() + BORDER_DIM, getStartY() + BORDER_DIM + g2.getFontMetrics().getAscent());
    }
}
