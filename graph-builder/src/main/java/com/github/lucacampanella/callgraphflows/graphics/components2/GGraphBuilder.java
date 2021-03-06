package com.github.lucacampanella.callgraphflows.graphics.components2;

import com.github.lucacampanella.callgraphflows.graphics.utils.GUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GGraphBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GGraphBuilder.class);

    private static final int BORDER = 10;

    GTwoSidedContainerWithTitles gTwoSidedContainerWithTitles = new GTwoSidedContainerWithTitles();

    public GGraphBuilder(GTwoSidedContainerWithTitles twoSidedContainer) {
        this.gTwoSidedContainerWithTitles = twoSidedContainer;
    }

    public static GGraphBuilder fromAnalysisResult(AnalysisResult analysisResult) {
        return new GGraphBuilder(GTwoSidedContainerWithTitles.fromAnalysisResult(analysisResult));
    }

    public void drawToFile(String path) throws IOException {

        SVGGraphics2D g2 = new SVGGraphics2D(10000, 10000);
        int width = gTwoSidedContainerWithTitles.getWidth(g2) + 2*BORDER;
        int height = gTwoSidedContainerWithTitles.getHeight(g2) + 2*BORDER;
        gTwoSidedContainerWithTitles.setParentsForFileDrawing();
        LOGGER.trace("{}", gTwoSidedContainerWithTitles);

        g2 = new SVGGraphics2D(width, height);
        GUtils.fillWithColor(g2, new Rectangle(0,0,width, height), Color.WHITE);

        gTwoSidedContainerWithTitles.draw(g2, BORDER,BORDER);

        String svgElement = g2.getSVGElement();

        Files.write(Paths.get(path), svgElement.getBytes());
    }
}
