package com.github.lucacampanella.callgraphflows.graphics.svg.components;

import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Graphical representation of an if-else statement, even with more elses
 */
public class GIfElse extends GBaseGraphicComponent {

    private static final int INDENTATION = GSubFlow.INDENTATION/2;
    private static final int SPACE_BETWEEN_COMPONENTS = GSubFlow.SPACE_BETWEEN_COMPONENTS;

    private final List<GConditionalBranch> blocks = new ArrayList<>();

    public void addBlock(GBaseTextComponent condition, Branch branch) {
        GConditionalBranch block = new GConditionalBranch(condition);
        block.setParent(this);
        blocks.add(block);
        branch.getStatements().forEach(stmt -> {
            if(stmt.getGraphElem() != null) {
                block.addComponent(stmt.getGraphElem());
            }
        });
    }

    @Override
    public void draw(SVGGraphics2D g2) {
        int currY = getStartY() + SPACE_BETWEEN_COMPONENTS;

        for(GBaseGraphicComponent comp : blocks) {
            comp.setStart(getStartX(), currY);
            comp.draw(g2);
            currY += comp.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS;
        }
    }

    @Override
    public String getPUMLString() {
        StringBuilder sb = new StringBuilder();
        for(GBaseGraphicComponent comp : blocks) {
            sb.append(comp.getPUMLString());
        }

        return sb.toString();
    }

    @Override
    public Dimension computeDimensions(SVGGraphics2D g2) {
        Dimension res = new Dimension();

        res.height = SPACE_BETWEEN_COMPONENTS;

        res.height += blocks.stream()
                .mapToInt(comp -> comp.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS).sum();

        res.width = INDENTATION + blocks.stream()
                                .mapToInt(comp -> comp.getDimensions(g2).width).max().orElse(0);

        return res;
    }

    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        super.drawBrothersAndLinks(g2);
        blocks.forEach(comp -> comp.drawBrothersAndLinks(g2));
    }

    public List<String> getCounterpartiesSessionNames() {
        return blocks.stream().map(comp -> comp.getCounterpartiesSessionNames()).flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public void setSessionName(String sessionName) {
        super.setSessionName(sessionName);
        blocks.forEach(block -> block.setSessionName(sessionName));
    }
}
