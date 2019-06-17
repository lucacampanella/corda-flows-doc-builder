package com.github.lucacampanella.callgraphflows.graphics.utils;

import com.github.lucacampanella.callgraphflows.graphics.components.GBaseGraphicComponent;
import com.github.lucacampanella.callgraphflows.graphics.preferences.PreferencesInterface;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;

public class GUtils {

    private GUtils() {
        //hides public constructor
    }

    public static void drawArrow(Graphics2D g2d, Line2D.Double line) {
        drawArrowLine(g2d, (int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2, 7, 4);
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
    private static void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
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

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
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
}
