package com.github.lucacampanella.callgraphflows.graphics.components2;

import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.util.ArrayList;
import java.util.List;

public class GIfElseIndented extends GBaseContainer {


    private static class GConditionalBranchIndentedWithRelativeY extends ComponentWithRelativeY {
        private GConditionalBranchIndented comp = null;

        public GConditionalBranchIndentedWithRelativeY(GConditionalBranchIndented comp) {
            this.comp = comp;
        }

        @Override
        public GConditionalBranchIndented getComp() {
            return comp;
        }
    }

    private static final int INDENTATION = GBaseIndentedContainer.INDENTATION/2;
    private static final int SPACE_BETWEEN_COMPONENTS = GBaseIndentedContainer.SPACE_BETWEEN_COMPONENTS;

    private final List<GConditionalBranchIndentedWithRelativeY> blocks = new ArrayList<>();

    private int currBlockIndex = 0;

    private void resetDrawingInfo() {
        currBlockIndex = 0;
    }


    public void addBlock(GBaseText condition, Branch branch) {
        GConditionalBranchIndented block = new GConditionalBranchIndented(condition);
        block.setParent(this);
        blocks.add(new GConditionalBranchIndentedWithRelativeY(block));
        branch.getStatements().forEach(stmt -> {
            if(stmt.getGraphElem() != null) {
                block.addComponent(stmt.getGraphElem());
            }
        });
    }

    @Override
    public void draw(SVGGraphics2D g2, int x, int y) {
        for(GConditionalBranchIndentedWithRelativeY comp : blocks) {
            comp.drawRelative(g2, x, y);
        }
    }

    @Override
    public ComponentWithRelativeY setUpDimensions(SVGGraphics2D g2, ComponentWithRelativeY lastCompWithNewY) {
        int currY = 0;
        if(lastCompWithNewY != null) {
            final GConditionalBranchIndentedWithRelativeY currBlockWithY = blocks.get(currBlockIndex);
            final GConditionalBranchIndented currBlock = currBlockWithY.getComp();
            if(!lastCompWithNewY.getComp().isSimpleComponent()) {
                throw new IllegalArgumentException("You didn't pass a simple component");
            }
            final ComponentWithRelativeY componentWithRelativeY =
                    currBlock.setUpDimensions(g2, lastCompWithNewY.subtractingToY(
                            currBlockWithY.getY()));

            if(componentWithRelativeY == null) { //there were no (more) blocking components
                currY = currBlockWithY.getY() + currBlock.getHeight(g2) + SPACE_BETWEEN_COMPONENTS;
                currBlockIndex++;
            }
            else {
                return componentWithRelativeY.addingToY(currBlockWithY.getY());
                //this is blocking and need to be further analyzed upwards
            }
        }
        while(currBlockIndex < blocks.size()) {
            final GConditionalBranchIndentedWithRelativeY currBlockWithY = blocks.get(currBlockIndex);
            final GConditionalBranchIndented currBlock = currBlockWithY.getComp();
            currBlockWithY.setY(currY);
            final ComponentWithRelativeY currBlockRes = currBlock.setUpDimensions(g2, null);
            if(currBlockRes != null) {
                return currBlockRes.addingToY(currBlockWithY.getY());
            }
            currBlockIndex++;
            currY += currBlock.getHeight(g2);
        }
        resetDrawingInfo();
        return null;
    }

    @Override
    public ComponentWithRelativeY setUpDimensionsUntilInitiateFlow(SVGGraphics2D g2, GBaseText initiateFlowComp) {
        int currY = 0;
        while(currBlockIndex < blocks.size()) {
            final GConditionalBranchIndentedWithRelativeY currBlockWithY = blocks.get(currBlockIndex);
            final GConditionalBranchIndented currBlock = currBlockWithY.getComp();
            currY += SPACE_BETWEEN_COMPONENTS;
            currBlockWithY.setY(currY);
            final ComponentWithRelativeY currBlockRes = currBlock.setUpDimensionsUntilInitiateFlow(g2, initiateFlowComp);
            if(currBlockRes != null) {
                return currBlockRes.addingToY(currBlockWithY.getY());
            }
            currBlockIndex++;
            currY += currBlock.getHeight(g2);
        }
        resetDrawingInfo();
        return null;
    }

    @Override
    protected int computeHeight(SVGGraphics2D g2) {
        return blocks.get(blocks.size()-1).getY() + blocks.get(blocks.size()-1).getComp().getHeight(g2);
    }

    @Override
    protected int computeWidth(SVGGraphics2D g2) {
        int maxWidth = 0;
        for (GConditionalBranchIndentedWithRelativeY block : blocks) {
            maxWidth = Math.max(maxWidth, block.getComp().getWidth(g2));
        }
        return maxWidth;
    }

    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        blocks.forEach(comp -> comp.getComp().drawBrothersAndLinks(g2));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(GConditionalBranchIndentedWithRelativeY block : blocks) {
            sb.append(block.toString());
            sb.append("\n-----\n");
        }
        return sb.toString();
    }
}
