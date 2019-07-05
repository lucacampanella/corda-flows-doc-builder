package com.github.lucacampanella.callgraphflows.graphics.components2;

import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public abstract class GBaseIndentedContainer extends GBaseContainer {

    protected static class GBaseTextWithRelativeY extends ComponentWithRelativeY {
        private GBaseText comp = null;

        public GBaseTextWithRelativeY(GBaseText comp) {
            this.comp = comp;
        }

        @Override
        public GBaseText getComp() {
            return comp;
        }

        public void setBaseText(GBaseText comp) {
            this.comp = comp;
        }
    }


    public static final int WIDTH = 30;
    static final int INDENTATION = WIDTH/2;
    static final int SPACE_BETWEEN_COMPONENTS = WIDTH/3;

    protected List<ComponentWithRelativeY> components = new ArrayList<>();
    protected GBaseTextWithRelativeY enteringArrowText = null;
    protected GBaseTextWithRelativeY exitingArrowText = null;

    int currCompIndex = -2; //-2 = not started, -1 == enteringArrowText, 0 == first component...

    private void resetDrawingInfo() {
        currCompIndex = -2;
    }

    public GBaseIndentedContainer() {

    }

    public GBaseIndentedContainer(GBaseText enteringArrowText, GBaseText exitingArrowText) {
        if(enteringArrowText != null) {
            this.enteringArrowText = new GBaseTextWithRelativeY(enteringArrowText);
        }
        if(exitingArrowText != null) {
            this.exitingArrowText = new GBaseTextWithRelativeY(exitingArrowText);
        }
    }


    public void addComponent(GBaseComponent component) {
        if(component != null) {
            components.add(new ComponentWithRelativeY(component));
            component.setParent(this);
        }
    }

    @Override
    public void draw(SVGGraphics2D g2, int x, int y) {
        int parentRightBorderX = x + WIDTH - INDENTATION;
        int arrowRightBorderX = x + WIDTH + WIDTH/2;

        int rectStart = y;
        int rectEnd = y + height;
        //int rectHeight = height;

        if(enteringArrowText != null) {
            int startArrowY = y + enteringArrowText.getY();
            int verticalHeight = enteringArrowText.getComp().getHeight(g2);
            g2.draw(new Line2D.Double(parentRightBorderX, startArrowY, arrowRightBorderX, startArrowY));
            g2.draw(new Line2D.Double(arrowRightBorderX, startArrowY, arrowRightBorderX,
                    (double) (startArrowY) + verticalHeight));

            GUtils.drawArrow(g2, new Line2D.Double(arrowRightBorderX, (double) (startArrowY) + verticalHeight,
                    (double) (x) + WIDTH, (double) (startArrowY) + verticalHeight));

            enteringArrowText.drawRelative(g2, arrowRightBorderX + INDENTATION/2, y);

            rectStart = startArrowY  + verticalHeight - SPACE_BETWEEN_COMPONENTS;
//            rectHeight -= enteringArrowText.getY();
//            rectHeight += SPACE_BETWEEN_COMPONENTS;
        }

        if(exitingArrowText != null) {
            rectEnd -= exitingArrowText.getComp().getHeight(g2);
            rectEnd += SPACE_BETWEEN_COMPONENTS;
        }
        final Rectangle rect = new Rectangle(x, rectStart, WIDTH, rectEnd-rectStart);

        GUtils.fillWithColor(g2, rect, getAwtColor());
        g2.draw(rect);
        //todo: draw it properly
//        if(enteringArrowText != null) {
//            enteringArrowText.drawRelative(g2, x, y);
//        }
        components.forEach(comp -> comp.drawRelative(g2, x + INDENTATION, y));
//        if(exitingArrowText != null) {
//            exitingArrowText.drawRelative(g2, x, y);
//        }

        if(exitingArrowText != null) {
            int startArrowY = y + exitingArrowText.getY();
            int verticalHeight = exitingArrowText.getComp().getHeight(g2);

            g2.draw(new Line2D.Double((double) (x) + WIDTH, startArrowY,
                    arrowRightBorderX, startArrowY));
            g2.draw(new Line2D.Double(arrowRightBorderX, startArrowY, arrowRightBorderX,
                    (double) (startArrowY) + verticalHeight));

            GUtils.drawArrow(g2, new Line2D.Double(arrowRightBorderX, (double) (startArrowY) + verticalHeight,
                    parentRightBorderX, (double) (startArrowY) + verticalHeight));


            exitingArrowText.drawRelative(g2, arrowRightBorderX + INDENTATION/2, y);
        }
    }

    @Override
    public ComponentWithRelativeY setUpDimensions(SVGGraphics2D g2, ComponentWithRelativeY lastCompWithNewY) {
        int currY = 0;
        if(currCompIndex < 0) { //we start computing here
            if(enteringArrowText != null) {
                if(enteringArrowText.getComp().hasAnyBrother() && currCompIndex < -1) {
                    enteringArrowText.setY(0);
                    currCompIndex = -1;
                    return enteringArrowText;
                }
                else {
                    if(currCompIndex < -1) {
                        enteringArrowText.setY(0);
                    }
                    currCompIndex = 0;
                    currY += enteringArrowText.getY() + enteringArrowText.getComp().getHeight(g2);
                }
            }
            else {
                currCompIndex = 0;
            }
        }
        else if(lastCompWithNewY != null) {        //analyze the object we got
            final ComponentWithRelativeY currCompWithY = components.get(currCompIndex);
            final GBaseComponent currComp = currCompWithY.getComp();
            if(lastCompWithNewY.getComp() == currComp) {
                //we are passed the object we are analyzing right now, this should happen only if it's a
                //simple object
                if(!lastCompWithNewY.getComp().isSimpleComponent()) {
                    throw  new IllegalArgumentException("You can't pass a container component here as argument");
                }

                currY = lastCompWithNewY.getY() + lastCompWithNewY.getComp().getHeight(g2);
                currCompIndex++;
            }
            else {
                //we are passed an object which we are not analyzing, must mean we are analyzing a container object
                //and the object passed is a simple one, part of this container
                if(!currComp.isContainerComponent()
                        || !lastCompWithNewY.getComp().isSimpleComponent()) {
                    throw  new IllegalArgumentException("You passed a simple object, it's not the one I'm analyzing" +
                            "and the one I'm analyzing is not a container object");
                }
                final ComponentWithRelativeY componentWithRelativeY =
                        ((GBaseContainer) currComp).setUpDimensions(g2, lastCompWithNewY.subtractingToY(
                                currCompWithY.getY()));
                if(componentWithRelativeY == null) { //there were no (more) blocking components
                    currY = currCompWithY.getY() + currComp.getHeight(g2);
                    currCompIndex++;
                }
                else {
                    return componentWithRelativeY.addingToY(currCompWithY.getY());
                    //this is blocking and need to be further analyzed upwards
                }
            }
        }
        //else we are in the situation in which setUpDimensionsUntilInitiateFlow was called and thus we already computed
        // a lot, but we arrive here with a null lastCompWithNewY

        if(currCompIndex == -1) { //we are still on the entering arrow text
            if(lastCompWithNewY == null) {
                throw new IllegalArgumentException("You passed a null object but we had just given back" +
                        "the entering arrow");
            }
            enteringArrowText.setY(lastCompWithNewY.getY());
            currY = lastCompWithNewY.getY() + enteringArrowText.getComp().getHeight(g2);
            currCompIndex = 0;
        }

        for(; currCompIndex < components.size(); ++currCompIndex) {
            final ComponentWithRelativeY currCompWithY = components.get(currCompIndex);
            final GBaseComponent currComp = currCompWithY.getComp();
            currY += SPACE_BETWEEN_COMPONENTS;
            currCompWithY.setY(currY);
            if(currComp.isContainerComponent()) {
                final ComponentWithRelativeY componentWithRelativeY =
                        ((GBaseContainer) currComp).setUpDimensions(g2, null);
                if(componentWithRelativeY == null) { //there were no blocking components
                    currY += currComp.getHeight(g2);
                }
                else {
                    return componentWithRelativeY.addingToY(currCompWithY.getY());
                    //this is blocking and need to be further analyzed upwards
                }
            }
            else if(currComp.isSimpleComponent()) {
                if(((GBaseSimpleComponent) currComp).hasAnyBrother()) {
                    return currCompWithY;
                }
                else {
                    currY += currComp.getHeight(g2);
                }
            }
            else if(currComp.isTwoSidedComponent()) {
                currY += currComp.getHeight(g2); //get height will trigger the internal process in this one
            }
        }
//        if(!components.isEmpty()) {
//            currY += components.get(components.size()-1).getComp().getHeight(g2);
//        }
        currY += SPACE_BETWEEN_COMPONENTS;
        //if we arrive here it means we have finished all components
        if(exitingArrowText != null) {
            if(exitingArrowText.getComp().hasAnyBrother()) {
                if(lastCompWithNewY.getComp() == exitingArrowText.getComp()) {
                    currY = exitingArrowText.getY() + exitingArrowText.getComp().getHeight(g2);
                }
                else {
                    exitingArrowText.setY(currY);
                    return exitingArrowText;
                }
            }
            else {
                exitingArrowText.setY(currY);
                currY += exitingArrowText.getComp().getHeight(g2);
            }
        }
        height = currY;
        resetDrawingInfo();
        return null;
    }

    @Override
    public ComponentWithRelativeY setUpDimensionsUntilInitiateFlow(SVGGraphics2D g2, GBaseText initiateFlowComp) {
        int currY = 0;
        if(enteringArrowText != null) {
            enteringArrowText.setY(0);
            if(enteringArrowText.getComp().hasAnyBrother()) {
                throw new IllegalStateException("The entering arrow text has brothers before the initiate flow call");
            }
            currY += enteringArrowText.getComp().getHeight(g2);
        }

        for(currCompIndex = 0; currCompIndex < components.size(); ++currCompIndex) {
            final ComponentWithRelativeY currCompWithY = components.get(currCompIndex);
            final GBaseComponent currComp = currCompWithY.getComp();
            currY += SPACE_BETWEEN_COMPONENTS;
            currCompWithY.setY(currY);
            if(currComp.isContainerComponent()) {
                    final ComponentWithRelativeY initiateFlow =
                            ((GBaseContainer) currComp).setUpDimensionsUntilInitiateFlow(g2, initiateFlowComp);
                    if(initiateFlow == null) { //this subflow doesn't contain the initiateFlowInstr
                        currY += currComp.getHeight(g2);
                    }
                    else {
                        return initiateFlow.addingToY(currCompWithY.getY());
                    }
            }
            else if(currComp.isSimpleComponent()) {
                if(currComp == initiateFlowComp) {
                    return currCompWithY; //todo: adding to?
                }
                if(((GBaseSimpleComponent) currComp).hasAnyBrother()) {
                    throw new IllegalStateException("Blocking component " +
                            currCompWithY.getComp() + " before the initiate flow call");
                }
                else {
                    currY += currComp.getHeight(g2);
                }
            }
            else if(currComp.isTwoSidedComponent()) {
                currY += currComp.getHeight(g2); //get height will trigger the internal process in this one
            }
        }

//        throw new IllegalArgumentException("Checked all the components, but no initiate flow found," +
//                "probably this is not an initiating flow");

        if(exitingArrowText != null) {
            if(exitingArrowText.getComp().hasAnyBrother()) {
                throw new IllegalStateException("The exiting arrow text has brothers before the initiate flow call");
            }
            currY += exitingArrowText.getComp().getHeight(g2);
        }
        height = currY;
        resetDrawingInfo();
        return null; //initiateFlow not found
    }

    @Override
    protected int computeHeight(SVGGraphics2D g2) {
        return height; //the height is computed by the method setUpDimensions
    }

    @Override
    public void drawBrothersAndLinks(SVGGraphics2D g2) {
        if(enteringArrowText != null) {
            enteringArrowText.getComp().drawBrothersAndLinks(g2);
        }
        components.forEach(comp -> comp.getComp().drawBrothersAndLinks(g2));
        if(exitingArrowText != null) {
            exitingArrowText.getComp().drawBrothersAndLinks(g2);
        }
    }

    public void setEnteringArrowText(GBaseText enteringArrowText) {
        if (enteringArrowText != null) {
            this.enteringArrowText = new GBaseTextWithRelativeY(enteringArrowText);
        }
        else {
            this.enteringArrowText = null;
        }
        recomputeDims();
    }

    public void setExitingArrowText(GBaseText exitingArrowText) {
        if(exitingArrowText != null) {
            this.exitingArrowText = new GBaseTextWithRelativeY(exitingArrowText);
        }
        else {
            this.exitingArrowText = null;
        }
        recomputeDims();
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

    public GBaseText getEnteringArrowText() {
        return enteringArrowText.getComp();
    }

    public GBaseText getExitingArrowText() {
        return exitingArrowText.getComp();
    }

    public int getRectStartOffset(SVGGraphics2D g2) {
        return enteringArrowText == null ?
                0 : enteringArrowText.getY() + enteringArrowText.getComp().getHeight(g2) - SPACE_BETWEEN_COMPONENTS;
    }

    public int getRealStart(SVGGraphics2D g2) {
        return enteringArrowText == null ?
                0 : enteringArrowText.getY();
    }

    @Override
    protected int computeWidth(SVGGraphics2D g2) {
        int maxWidth = 0;
        if(enteringArrowText != null) {
            maxWidth = Math.max(maxWidth, enteringArrowText.getComp().getWidth(g2) + WIDTH + WIDTH/2); //todo
        }
        for (ComponentWithRelativeY comp : components) {
            maxWidth = Math.max(maxWidth, comp.getComp().getWidth(g2) + INDENTATION);
        }
        if(exitingArrowText != null) {
            maxWidth = Math.max(maxWidth, exitingArrowText.getComp().getWidth(g2) + WIDTH + WIDTH/2);
        }
        return maxWidth;
    }

    protected abstract Color getAwtColor();
}
