package com.github.lucacampanella.callgraphflows.asciidoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AsciiDocIndexBuilder {
    List<String> asciiDocFileNames = new ArrayList<>();
    String analysisName;

    public AsciiDocIndexBuilder(String analysisName) {
        this.analysisName = analysisName;
    }

    public void addFile(String fileName) {
        asciiDocFileNames.add(fileName);
    }

    public void writeToFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Analysis of ");
        sb.append(analysisName);
        sb.append("\n\n");
        for (String fileName : asciiDocFileNames) {
            sb.append("include::");
            sb.append(fileName);
            sb.append("[]\n");
        }

        Files.write(Paths.get(path), sb.toString().getBytes());
    }
}
