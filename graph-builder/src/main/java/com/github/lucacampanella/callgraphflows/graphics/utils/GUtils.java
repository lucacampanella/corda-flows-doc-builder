package com.github.lucacampanella.callgraphflows.graphics.utils;

import com.github.lucacampanella.callgraphflows.graphics.components.GBaseGraphicComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.graphics.preferences.PreferencesInterface;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;

public class GUtils {

    private static final double DEFAULT_BRIGHTER_FACTOR = 1.2;
    public static final Stroke DASHED_STROKE =
            new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);

    public static final Color VERY_LIGHT_GRAY = new Color(235, 235, 235);


    private GUtils() {
        //hides public constructor
    }

    public static void drawArrow(SVGGraphics2D g2d, Line2D.Double line) {
        drawArrowLine(g2d, (int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2, 7, 4, null, null);
    }

    public static void drawArrowWithOptions(SVGGraphics2D g2d, Line2D.Double line, Color color, Stroke stroke) {
        drawArrowLine(g2d, (int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2, 7, 4, color, stroke);
    }


    public static void drawHorizontalArrowFromFirstToSecond(SVGGraphics2D g2d, GBaseGraphicComponent first, GBaseGraphicComponent second) {
        int firstX;
        int secondX;

        if(first.getMiddleX(g2d) < second.getMiddleX(g2d)) {
            firstX = first.getRightBorder(g2d);
            secondX = second.getStartX();
        } else {
            firstX = first.getStartX();
            secondX = second.getRightBorder(g2d);
        }

        drawArrow(g2d, new Line2D.Double(firstX, first.getMiddleY(g2d), secondX, second.getMiddleY(g2d)));
    }

    /**
     * Draw an arrow line between two points.
     * @param g the graphics component.
     * @param x1 x-position of first point.
     * @param y1 y-position of first point.
     * @param x2 x-position of second point.
     * @param y2 y-position of second point.
     * @param d  the width of the arrow.
     * @param h  the height of the arrow.
     */
    private static void drawArrowLine(SVGGraphics2D g, int x1, int y1, int x2, int y2, int d, int h, Color color, Stroke stroke) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        double f = Math.sqrt((double) (dx*dx) + dy*dy);
        double xm = f - d;
        double xn = xm;
        double ym = h;
        double yn = -h;
        double x;
        double sin = dy / f;
        double cos = dx / f;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        //g.drawLine(x1, y1, x2, y2);
        drawLineWithOptions(g, x1, y1, x2, y2, color, stroke);
        Color defaultColor = g.getColor();
        if(color != null) {
            g.setColor(color);
        }
        g.fillPolygon(xpoints, ypoints, 3);
        g.setColor(defaultColor);
    }
    public static int doubleToInt(double d) {
        return (int) Math.round(d+1.0);
    }

    public static void fillWithColor(SVGGraphics2D g2, Shape shape, Color color) {
        Color colorBackup = g2.getColor();
        g2.setColor(color);
        g2.fill(shape);
        g2.setColor(colorBackup);
    }

    public static void drawColoredShape(SVGGraphics2D g2, Shape shape, Color color) {
        Color colorBackup = g2.getColor();
        g2.setColor(color);
        g2.draw(shape);
        g2.setColor(colorBackup);
    }

    public static void drawColoredShapeWithStroke(SVGGraphics2D g2, Shape shape, Color color, Stroke stroke) {
        Stroke strokeBackup = g2.getStroke();
        g2.setStroke(stroke);
        drawColoredShape(g2, shape, color);
        g2.setStroke(strokeBackup);
    }

    public static Color makeBrighter(PreferencesInterface pref, Color color) {
        double r;
        double g;
        double b;
        r = Math.min(255.0,color.getRed()*pref.getBrighterFactor());
        g = Math.min(255.0,color.getGreen()*pref.getBrighterFactor());
        b = Math.min(255.0, color.getBlue()*pref.getBrighterFactor());
        return new Color((int) r, (int) g, (int) b);
    }

    public static Color makeBrighter(double brighterFactor, Color color) {
        double r;
        double g;
        double b;
        r = Math.min(255.0,color.getRed()*brighterFactor);
        g = Math.min(255.0,color.getGreen()*brighterFactor);
        b = Math.min(255.0, color.getBlue()*brighterFactor);
        return new Color((int) r, (int) g, (int) b);
    }
    public static Color makeBrighter(Color color) {
        return makeBrighter(DEFAULT_BRIGHTER_FACTOR, color);
    }

    public static void drawLineWithOptions(Graphics2D g2, int startX, int startY, int endX, int endY,
                                           Color color, Stroke stroke) {
        Color defaultColor = g2.getColor();
        Stroke defaultStroke = g2.getStroke();
        if(color != null) {
            g2.setColor(color);
        }
        if(stroke != null) {
            g2.setStroke(stroke);
        }
        g2.drawLine(startX, startY, endX, endY);

        g2.setColor(defaultColor);
        g2.setStroke(defaultStroke);
    }
}
