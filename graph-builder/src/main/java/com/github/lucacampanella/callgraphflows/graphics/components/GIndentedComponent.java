package com.github.lucacampanella.callgraphflows.graphics.components;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.LinkedList;
import java.util.List;

public abstract class GIndentedComponent extends GBaseGraphicComponent {
    public static final int WIDTH = 30;
    static final int INDENTATION = WIDTH/2;
    static final int SPACE_BETWEEN_COMPONENTS = WIDTH/3;

    protected List<GBaseGraphicComponent> components = new LinkedList<>();
    protected GBaseTextComponent enteringArrowText;
    protected GBaseTextComponent exitingArrowText;

    private Dimension rectDimensions;

    public GIndentedComponent(GBaseTextComponent enteringArrowText) {
        this(enteringArrowText, null);
    }

    public GIndentedComponent(GBaseTextComponent enteringArrowText, GBaseTextComponent exitingArrowText) {
        this.enteringArrowText = enteringArrowText;
        this.exitingArrowText = exitingArrowText;
    }

    public GIndentedComponent() {
        this(null, null);
    }

    public void addComponent(GBaseGraphicComponent component) {
        if(component != null) {
            components.add(component);
            component.setParent(this);
        }
    }

    @Override
    public void draw(SVGGraphics2D g2) {
        final Dimension dimensions = getRectDimensions(g2);

        Rectangle rect;
        int currStartY = getStartY();

        int parentRightBorderX = getStartX() + WIDTH - INDENTATION;
        int arrowRightBorderX = getStartX() + WIDTH + WIDTH/2;

        if(enteringArrowText != null) {
            g2.draw(new Line2D.Double(parentRightBorderX, getStartY(), arrowRightBorderX, getStartY()));
            g2.draw(new Line2D.Double(arrowRightBorderX, getStartY(), arrowRightBorderX,
                    (double) (getStartY()) + SPACE_BETWEEN_COMPONENTS * 2));
            GUtils.drawArrow(g2, new Line2D.Double(arrowRightBorderX, (double) (getStartY()) + SPACE_BETWEEN_COMPONENTS * 2,
                    (double) (getStartX()) + WIDTH, (double) (getStartY()) + SPACE_BETWEEN_COMPONENTS * 2));
            enteringArrowText.setStart(arrowRightBorderX + INDENTATION/2,
                    getStartY() + SPACE_BETWEEN_COMPONENTS - enteringArrowText.getDimensions(g2).height/2);
            enteringArrowText.draw(g2);

            currStartY += SPACE_BETWEEN_COMPONENTS;
        }
        rect = new Rectangle(getStartX(), currStartY, dimensions.width, dimensions.height);

        GUtils.fillWithColor(g2, rect, getAwtColor());
        g2.draw(rect);
        currStartY += SPACE_BETWEEN_COMPONENTS;
        if(enteringArrowText != null) {
            currStartY += SPACE_BETWEEN_COMPONENTS;
        }
        for(GBaseGraphicComponent comp : components) {
            comp.setStart(getStartX() + INDENTATION, currStartY);
            comp.draw(g2);
            currStartY += comp.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS;
        }

        if(exitingArrowText != null) {
            g2.draw(new Line2D.Double((double) (getStartX()) + WIDTH, currStartY, arrowRightBorderX, currStartY));
            g2.draw(new Line2D.Double(arrowRightBorderX, currStartY, arrowRightBorderX,
                    (double) (currStartY) + SPACE_BETWEEN_COMPONENTS * 2));
            GUtils.drawArrow(g2, new Line2D.Double(arrowRightBorderX, (double) (currStartY) + SPACE_BETWEEN_COMPONENTS * 2,
                    parentRightBorderX, (double) (currStartY) + SPACE_BETWEEN_COMPONENTS * 2));

            exitingArrowText.setStart( arrowRightBorderX+INDENTATION/2,
                    currStartY + SPACE_BETWEEN_COMPONENTS - exitingArrowText.getDimensions(g2).height/2);
            exitingArrowText.draw(g2);
        }
    }

    @Override
    public Dimension computeDimensions(SVGGraphics2D g2) {
        Dimension res = new Dimension(getRectDimensions(g2));

        res.width = 0;

        if(enteringArrowText != null) {
            res.height += SPACE_BETWEEN_COMPONENTS;
            res.width = Math.max(res.width, WIDTH + WIDTH/2+ INDENTATION/2 + enteringArrowText.getDimensions(g2).width);
        }

        if(exitingArrowText != null) {
            res.height += SPACE_BETWEEN_COMPONENTS;
            res.width = Math.max(res.width, WIDTH + WIDTH/2+ INDENTATION/2 + exitingArrowText.getDimensions(g2).width);
        }

        res.width = Math.max(res.width, INDENTATION + components.stream()
                .mapToInt(comp -> comp.getDimensions(g2).width).max().orElse(0));

        return res;
    }

    private Dimension getRectDimensions(SVGGraphics2D g2) {
        if(recomputeDimensions) {
            int height = SPACE_BETWEEN_COMPONENTS;

            if (enteringArrowText != null) {
                height += SPACE_BETWEEN_COMPONENTS;
            }

            height += components.stream().mapToInt(comp -> comp.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS).sum();

            if (exitingArrowText != null) {
                height += SPACE_BETWEEN_COMPONENTS;
            }

            rectDimensions = new Dimension(WIDTH, height);
        }
        return rectDimensions;
    }

    protected abstract Color getAwtColor();

    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        super.drawBrothersAndLinks(g2);
        if(enteringArrowText != null) {
            enteringArrowText.drawBrothersAndLinks(g2);
        }
        components.forEach(comp -> comp.drawBrothersAndLinks(g2));
        if(exitingArrowText != null) {
            exitingArrowText.drawBrothersAndLinks(g2);
        }
    }

    public void setEnteringArrowText(GBaseTextComponent enteringArrowText) {
        this.enteringArrowText = enteringArrowText;
    }

    public void setExitingArrowText(GBaseTextComponent exitingArrowText) {
        this.exitingArrowText = exitingArrowText;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(enteringArrowText != null) {
            sb.append(enteringArrowText.toString());
            sb.append("\n");
        }
        components.forEach(comp -> {
            sb.append("     ");
            sb.append(comp.toString());
            sb.append("\n");
        });
        if(exitingArrowText != null) {
            sb.append(exitingArrowText.toString());
        }

        return sb.toString();
    }

    public GBaseTextComponent getEnteringArrowText() {
        return enteringArrowText;
    }

    public GBaseTextComponent getExitingArrowText() {
        return exitingArrowText;
    }
}
