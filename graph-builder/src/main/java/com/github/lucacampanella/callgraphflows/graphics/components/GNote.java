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
        path.lineTo((double) (getStartX()) + dim.width, (double) (getStartY()) + dim.height);
        path.lineTo(getStartX(), (double) (getStartY()) + dim.height);
        path.closePath();

        GUtils.fillWithColor(g2, path, Color.WHITE);


        g2.draw(new Line2D.Double(getStartX(), getStartY(), foldingPointTopX, foldingPointTopY));
        g2.draw(new Line2D.Double(getStartX(), getStartY(), getStartX(), (double) (getStartY()) + dim.height));
        g2.draw(new Line2D.Double(getStartX(), (double) (getStartY()) + dim.height,
                getStartX() + dim.width, (double) (getStartY()) + dim.height));
        g2.draw(new Line2D.Double((double) (getStartX()) + dim.width, (double) (getStartY()) + dim.height,
                foldingPointRightX, foldingPointRightY));

        g2.draw(new Line2D.Double(foldingPointTopX, foldingPointTopY,
                foldingPointTopX, (double) (foldingPointTopY) + FOLD_DIM));
        g2.draw(new Line2D.Double(foldingPointRightX, foldingPointRightY,
                (double) (foldingPointRightX) - FOLD_DIM, foldingPointRightY));
        g2.draw(new Line2D.Double(foldingPointRightX, foldingPointRightY,
                foldingPointTopX, foldingPointTopY));

        g2.drawString(text, getStartX() + BORDER_DIM, getStartY() + BORDER_DIM + g2.getFontMetrics().getAscent());
    }
}
