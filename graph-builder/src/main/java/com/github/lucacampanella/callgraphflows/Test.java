package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.callgraphflows.graphics.components.*;
import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.io.IOException;

public class Test {

    public static void main(String []args) {

        testGraphLibrary();
    }

    public static void testGraphLibrary() {
        SVGGraphics2D g2 = new SVGGraphics2D(1500, 500);
        GUtils.fillWithColor(g2, new Rectangle(0,0,1500,500), Color.WHITE);


        GNote note = new GNote("this is a note");
        System.out.println(note.getDimensions(g2));

        GSubFlow flow = new GSubFlow();
        flow.addComponent(note);

        GSubFlow flow2 = new GSubFlow(new GBaseTextComponent("test text on arrow"), null);
        flow.addComponent(flow2);
        flow2.addComponent(new GNote("note"));

        flow2.addComponent(new GNote("this is another note"));

        GSubFlow flow3 = new GSubFlow(new GBaseTextComponent("this is another entering arrow"),
                new GBaseTextComponent("this an exiting string"));
        flow2.addComponent(flow3);

        flow3.addComponent(new GNote("this is another another note"));

        flow3.addComponent(new GInstruction(5, "SignedTransaction fullySignedTx = " +
                "subFlow(new CollectSignaturesFlow(partSignedTx, " +
                "ImmutableSet.of(otherPartySession), " +
                "CollectSignaturesFlow.Companion.tracker()))"));

        flow.setStart(10,10);
        flow.draw(g2);

        GGraphBuilder builder = new GGraphBuilder();
        builder.addSession("First test session", flow);

        try {
            builder.drawToFile("example/test/test.svg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
