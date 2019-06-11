package com.github.lucacampanella.callgraphflows.asciidoc;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AsciiDocBuilder {
    private String className;
    private String classQualifiedName;
    private String counterPartyClassName;
    private String counterPartyClassQualifiedName;
    private String comments;

    public AsciiDocBuilder(String className, String classQualifiedName, String counterPartyClassName, String counterPartyClassQualifiedName, String comments) {
        this.className = className;
        this.classQualifiedName = classQualifiedName;
        this.counterPartyClassName = counterPartyClassName;
        this.counterPartyClassQualifiedName = counterPartyClassQualifiedName;
        this.comments = comments;
    }

    public static AsciiDocBuilder fromAnalysisResult(AnalysisResult analysisResult) {
        if(!analysisResult.hasCounterpartyResult()) {
            return new AsciiDocBuilder(analysisResult.getClassSimpleName(), analysisResult.getClassFullyQualifiedName(),
                    null, null,
                    analysisResult.getClassCommentForDocumentation());
        }
        final AnalysisResult counterpartyClassResult = analysisResult.getCounterpartyClassResult();
        return new AsciiDocBuilder(analysisResult.getClassSimpleName(), analysisResult.getClassFullyQualifiedName(),
                counterpartyClassResult.getClassSimpleName(), counterpartyClassResult.getClassFullyQualifiedName(),
                analysisResult.getClassCommentForDocumentation());
    }

    public void writeToFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("## ");
        sb.append("Class " + className);
        sb.append("\n");
        if(counterPartyClassName == null) {
            sb.append("This class is a wrapper class, has no initiateFlow call, but calls other subflows. " +
                    "Thus this class has no direct counterparty class\n");
        }
        else {
            sb.append("This class calls initiateFlow directly and thus has a counterparty class named *");
            sb.append(counterPartyClassName);
            sb.append("*\n");
        }
        sb.append("\n");
        sb.append(comments);
        sb.append("\n");
        sb.append("image::");
        sb.append(classQualifiedName);
        sb.append(".svg[]\n");


        Files.write(Paths.get(path), sb.toString().getBytes());
    }
}
