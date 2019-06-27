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
        final Dimension rectDimensions = getRectDimensions(g2);

        Rectangle rect;
        int currStartY = getStartY();

        int parentRightBorderX = getStartX() + WIDTH - INDENTATION;
        int arrowRightBorderX = getStartX() + WIDTH + WIDTH/2;

        if(enteringArrowText != null) {
            final Dimension enteringArrowTextDimensions = enteringArrowText.getDimensions(g2);
            int verticalHeight = Math.max(SPACE_BETWEEN_COMPONENTS * 2, enteringArrowTextDimensions.height);

            g2.draw(new Line2D.Double(parentRightBorderX, getStartY(), arrowRightBorderX, getStartY()));
            g2.draw(new Line2D.Double(arrowRightBorderX, getStartY(), arrowRightBorderX,
                    (double) (getStartY()) + verticalHeight));

            GUtils.drawArrow(g2, new Line2D.Double(arrowRightBorderX, (double) (getStartY()) + verticalHeight,
                    (double) (getStartX()) + WIDTH, (double) (getStartY()) + verticalHeight));

            enteringArrowText.setStart(arrowRightBorderX + INDENTATION/2,
                    getStartY() + verticalHeight/2 - enteringArrowTextDimensions.height/2);
            enteringArrowText.draw(g2);

            currStartY += verticalHeight - SPACE_BETWEEN_COMPONENTS;
        }
        rect = new Rectangle(getStartX(), currStartY, rectDimensions.width, rectDimensions.height);

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
            final Dimension exitingArrowTextDimensions = exitingArrowText.getDimensions(g2);
            int verticalHeight = Math.max(SPACE_BETWEEN_COMPONENTS * 2, exitingArrowTextDimensions.height);

            g2.draw(new Line2D.Double((double) (getStartX()) + WIDTH, currStartY, arrowRightBorderX, currStartY));
            g2.draw(new Line2D.Double(arrowRightBorderX, currStartY, arrowRightBorderX,
                    (double) (currStartY) + verticalHeight));

            GUtils.drawArrow(g2, new Line2D.Double(arrowRightBorderX, (double) (currStartY) + verticalHeight,
                    parentRightBorderX, (double) (currStartY) + verticalHeight));

            exitingArrowText.setStart( arrowRightBorderX+INDENTATION/2,
                    currStartY + verticalHeight/2 - exitingArrowTextDimensions.height/2);
            exitingArrowText.draw(g2);
        }
    }

    @Override
    public Dimension computeDimensions(SVGGraphics2D g2) {
        Dimension res = new Dimension(getRectDimensions(g2));

        res.width = 0;

        addDimOfArrowText(g2, res, enteringArrowText);

        addDimOfArrowText(g2, res, exitingArrowText);

        res.width = Math.max(res.width, INDENTATION + components.stream()
                .mapToInt(comp -> comp.getDimensions(g2).width).max().orElse(0));

        return res;
    }

    private void addDimOfArrowText(SVGGraphics2D g2, Dimension res, GBaseTextComponent arrowText) {
        if(arrowText != null) {
            final Dimension exitingArrowTextDimensions = arrowText.getDimensions(g2);
            int verticalHeight = Math.max(SPACE_BETWEEN_COMPONENTS * 2, exitingArrowTextDimensions.height);

            res.height  += verticalHeight - SPACE_BETWEEN_COMPONENTS;
            res.width = Math.max(res.width, WIDTH + WIDTH/2+ INDENTATION/2 + arrowText.getDimensions(g2).width);
        }
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
        recomputeDimensions = true;
    }

    public void setExitingArrowText(GBaseTextComponent exitingArrowText) {
        this.exitingArrowText = exitingArrowText;
        recomputeDimensions = true;
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

    /**
     *
     * @param g2 the graphics
     * @return how many pixels the rectangle representing the flow will start after the
     * designed starting point due to the dimension of the entering arrow (if present)
     */
    public int getStartingRectOffset(SVGGraphics2D g2) {
        if(enteringArrowText != null) {
            return Math.max(SPACE_BETWEEN_COMPONENTS * 2, getEnteringArrowText().getDimensions(g2).height)
                    - SPACE_BETWEEN_COMPONENTS;
        }
        else {
            return 0;
        }
    }
}
