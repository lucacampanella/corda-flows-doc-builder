package com.github.lucacampanella.callgraphflows.graphics.components;

import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphical representation of an if-else statement, even with more elses
 */
public class GIfElse extends GBaseGraphicContainerComponent {

    private static final int INDENTATION = GSubFlow.INDENTATION/2;
    private static final int SPACE_BETWEEN_COMPONENTS = GSubFlow.SPACE_BETWEEN_COMPONENTS;

    private final List<GConditionalBranch> blocks = new ArrayList<>();

    private int currBlockIndex = 0;

    @Override
    public void resetDrawingInfo() {
        super.resetDrawingInfo();
        currBlockIndex = 0;
    }

//    public void addBlock(GBaseTextComponent condition, Branch branch) {
//        GConditionalBranch block = new GConditionalBranch(condition);
//        block.setParent(this);
//        blocks.add(block);
//        branch.getStatements().forEach(stmt -> {
//            if(stmt.getGraphElem() != null) {
//                block.addComponent(stmt.getGraphElem());
//            }
//        });
//    }

    @Override
    public void draw(SVGGraphics2D g2) {
        for(GBaseGraphicComponent comp : blocks) {
            comp.draw(g2);
        }
    }

//    @Override
//    public Dimension computeDimensions(SVGGraphics2D g2) {
//        Dimension res = new Dimension();
//
//        res.height += blocks.stream()
//                .mapToInt(comp -> comp.getDimensions(g2).height + SPACE_BETWEEN_COMPONENTS).sum()
//        - SPACE_BETWEEN_COMPONENTS; //the last one should not have border computed here
//
//        res.width = INDENTATION + blocks.stream()
//                                .mapToInt(comp -> comp.getDimensions(g2).width).max().orElse(0);
//
//        return res;
//    }

    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        super.drawBrothersAndLinks(g2);
        blocks.forEach(comp -> comp.drawBrothersAndLinks(g2));
    }

    @Override
    public void setStartX(int startX) {
        super.setStartX(startX);
        blocks.forEach(block -> block.setStartX(startX));
    }

    @Override
    public GBaseGraphicComponent computeDimsStartingFrom(SVGGraphics2D g2, GBaseGraphicComponent comp) {
        if(comp == null) { //just start computing
            resetDrawingInfo();
            if(!blocks.isEmpty()) {
                blocks.get(0).setStart(getStartX(), getStartY());
            }
        }
        while(currBlockIndex < blocks.size()) {
            final GConditionalBranch currBlock = blocks.get(currBlockIndex);
            final GBaseGraphicComponent currBlockRes = currBlock.computeDimsStartingFrom(g2, comp);
            if(currBlockRes != null) {
                return currBlockRes;
            }
            currBlockIndex++;
            addToCurrY(currBlock.getHeight(g2));
            if(currBlockIndex < blocks.size()) {
                blocks.get(currBlockIndex).setStart(getStartX(), getCurrY());
            }
        }
        return null;
    }

    @Override
    protected int computeWidth(SVGGraphics2D g2) {
        int maxWidth = 0;
        for (GConditionalBranch block : blocks) {
            maxWidth = Math.max(maxWidth, block.getWidth(g2));
        }
        return maxWidth;
    }


}
