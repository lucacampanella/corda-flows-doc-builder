package com.github.lucacampanella.callgraphflows.graphics.svg.components;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class GComponentsList extends GBaseGraphicComponent {
    private static final int SPACE_BETWEEN_COMPONENTS = GSubFlow.SPACE_BETWEEN_COMPONENTS;

    private List<GBaseGraphicComponent> components = new LinkedList<>();

    public GComponentsList(GBaseGraphicComponent component) {
        components.add(component);
    }

    @Override
    public void draw(SVGGraphics2D g2) {
        int currStartY = getStartY();
        for(GBaseGraphicComponent comp : components) {
            comp.setStart(getStartX(), currStartY);
            comp.draw(g2);
            currStartY += comp.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS;
        }
    }

    @Override
    public Dimension computeDimensions(SVGGraphics2D g2) {
        Dimension res = new Dimension();

        res.height = components.stream()
                .mapToInt(comp -> comp.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS).sum();
        res.width = Math.max(res.width, components.stream()
                .mapToInt(comp -> comp.getDimensions(g2).width).max().orElse(0));

        return res;
    }


    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        super.drawBrothersAndLinks(g2);
        components.forEach(comp -> comp.drawBrothersAndLinks(g2));
    }

    @Override
    public String getPUMLString() {
        return null; //todo if class wants to be used
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        components.forEach(comp -> {
            sb.append(comp.toString());
            sb.append("\n");
        });

        return sb.toString();
    }
}
