package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String []args) throws IOException {

        JarAnalyzer analyzer;

        if(args.length > 0) {
            analyzer = new JarAnalyzer(args[0]);
        }
        else {
            LOGGER.error("Please provide " +
                    "jar parth as first argument and optionally out folder for graphs as second");
            return;
        }

        if(args.length > 1) {
            Drawer.drawAllStartableClasses(analyzer, args[1]);
        }
        else {
            Drawer.drawAllStartableClasses(analyzer);
        }
    }
}
