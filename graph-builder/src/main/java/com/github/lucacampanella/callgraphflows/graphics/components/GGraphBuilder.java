package com.github.lucacampanella.callgraphflows.graphics.components;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class GGraphBuilder {

    private static final int BORDER = 10;
    private static final int DISTANCE_TITLE_SESSION = 25;

    Map<String, GSubFlow> gSessionsMap = new LinkedHashMap<>();
    //linked so we can iterate over it on same order of insertion

    public GGraphBuilder() {
        //empty constructor
    }

    public static GGraphBuilder fromAnalysisResult(AnalysisResult analysisResult) {
        GGraphBuilder graphBuilder = new GGraphBuilder();

        graphBuilder.addSession(analysisResult.getClassSimpleName(), analysisResult.getStatements());
        final AnalysisResult initiatedClassResult = analysisResult.getCounterpartyClassResult();
        if(initiatedClassResult != null) {
            graphBuilder.addSession(initiatedClassResult.getClassSimpleName(), initiatedClassResult.getStatements());
        }

        return graphBuilder;
    }

    public boolean addSession(String title, GSubFlow flow) {
        if(gSessionsMap.containsKey(title) || gSessionsMap.containsValue(flow)) {
            return false;
        }
        gSessionsMap.put(title, flow);
        return true;
    }

    public boolean addSession(String title, Branch  branch) {
        if(gSessionsMap.containsKey(title)) {
            return false;
        }
        GSubFlow flow = new GSubFlow();

        for(StatementInterface instr : branch.getStatements()) {
            if(instr.getGraphElem() != null) {
                flow.addComponent(instr.getGraphElem());
            }
        }

        gSessionsMap.put(title, flow);
        return true;
    }

    public GSubFlow getOrCreateSession(String title) {
        if(gSessionsMap.containsKey(title)) {
            return gSessionsMap.get(title);
        }
        GSubFlow flow = new GSubFlow();
        gSessionsMap.put(title, flow);

        return flow;
    }

    public boolean addToSession(String sessionTitle, GBaseGraphicComponent component) {
        if(!gSessionsMap.containsKey(sessionTitle)) {
            return false;
        }
        gSessionsMap.get(sessionTitle).addComponent(component);

        return true;
    }

    public void drawToFile(String path) throws IOException {

        int width = 0;
        int height = 0;
        SVGGraphics2D g2 = new SVGGraphics2D(10000, 10000);

        List<GTitleBox> titleBoxes = new ArrayList<>(gSessionsMap.size());

        for(Map.Entry<String, GSubFlow> entry : gSessionsMap.entrySet()) {
            String title = entry.getKey();
            GTitleBox titleBox = new GTitleBox(title);
            titleBoxes.add(titleBox);

            Dimension dim = entry.getValue().getDimensions(g2);
            width += Math.max(dim.width, titleBox.getDimensions(g2).width);
            if(dim.height > height) {
                height = dim.height + titleBox.getDimensions(g2).height + BORDER;
            }
        }

        width += BORDER + gSessionsMap.size()*BORDER;
        height += BORDER*3;

        g2 = new SVGGraphics2D(width, height);

        GUtils.fillWithColor(g2, new Rectangle(0,0,width,height), Color.WHITE);

        int currentX = BORDER;
        final Iterator<GTitleBox> titleBoxIterator = titleBoxes.iterator();
        for (Map.Entry<String, GSubFlow> entry : gSessionsMap.entrySet()) {
            GSubFlow session = entry.getValue();

            final GTitleBox titleBox = titleBoxIterator.next();
            titleBox.setStart(currentX, BORDER);
            titleBox.draw(g2);

            //draw the dashed line connecting the title to the session:
            //set the stroke of the copy, not the original
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
            Stroke defaultStroke = g2.getStroke();
            g2.setStroke(dashed);
            g2.drawLine(currentX + GSubFlow.WIDTH/2, BORDER + titleBox.getDimensions(g2).height,
                    currentX + GSubFlow.WIDTH/2,
                    BORDER + titleBox.getDimensions(g2).height + DISTANCE_TITLE_SESSION);
            g2.setStroke(defaultStroke);

            session.setStart(currentX,BORDER + titleBox.getDimensions(g2).height + DISTANCE_TITLE_SESSION);
            session.draw(g2);
            currentX += Math.max(session.getDimensions(g2).width, titleBox.getDimensions(g2).width) + BORDER;

        }

        for (Map.Entry<String, GSubFlow> entry : gSessionsMap.entrySet()) {
            GSubFlow session = entry.getValue();
            session.drawBrothersAndLinks(g2);
        }

        String svgElement = g2.getSVGElement();

        Files.write(Paths.get(path), svgElement.getBytes());

    }
}
