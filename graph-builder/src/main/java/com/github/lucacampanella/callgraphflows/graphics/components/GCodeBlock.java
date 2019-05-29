package com.github.lucacampanella.callgraphflows.graphics.components;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.LinkedList;
import java.util.List;

/**
 * For now not used, todo: eliminate if not used
 */
public class GCodeBlock extends GBaseGraphicComponent {

    private static final int INDENTATION = GSubFlow.INDENTATION/2;
    private static final int SPACE_BETWEEN_COMPONENTS = GSubFlow.SPACE_BETWEEN_COMPONENTS;

    private GInstruction enteringInstruction = null;

    private List<GBaseGraphicComponent> components = new LinkedList<>();


    public GCodeBlock(GInstruction enteringInstruction) {
        this.enteringInstruction = enteringInstruction;
    }

    public GCodeBlock() {
        this(null);
    }

    public void setEnteringInstruction(GInstruction instruction) {
        enteringInstruction = instruction;
    }

    public void addComponent(GBaseGraphicComponent component) {
        if(component != null) {
            components.add(component);
            component.setParent(this);
        }
    }

    @Override
    public void draw(SVGGraphics2D g2) {
        int currY = getStartY() + SPACE_BETWEEN_COMPONENTS;
        int startingLineY = currY;
        int indentedX = getStartX() + INDENTATION;
        if(enteringInstruction != null) {
            enteringInstruction.setStart(getStartX(), currY);
            enteringInstruction.draw(g2);
            currY += enteringInstruction.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS;
            startingLineY = currY - SPACE_BETWEEN_COMPONENTS;
        }


        for(GBaseGraphicComponent comp : components) {
            comp.setStart(indentedX, currY);
            comp.draw(g2);
            currY += comp.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS;
        }

        GUtils.drawColoredShapeWithStroke(g2, new Line2D.Double(getStartX(), startingLineY,
                getStartX(), currY - SPACE_BETWEEN_COMPONENTS), Color.WHITE, new BasicStroke(2));
    }

    @Override
    public Dimension computeDimensions(SVGGraphics2D g2) {
        Dimension res = new Dimension();

        res.height = SPACE_BETWEEN_COMPONENTS;

        res.height += enteringInstruction == null ? 0 :
                enteringInstruction.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS;

        res.height += components.stream()
                .mapToInt(comp -> comp.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS).sum();

        res.width = INDENTATION +
                Math.max(enteringInstruction == null ? 0 : enteringInstruction.getDimensions(g2).width,
                        components.stream()
                .mapToInt(comp -> comp.getDimensions(g2).width).max().orElse(0));

        return res;
    }


    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        super.drawBrothersAndLinks(g2);
        components.forEach(comp -> comp.drawBrothersAndLinks(g2));
    }
}
