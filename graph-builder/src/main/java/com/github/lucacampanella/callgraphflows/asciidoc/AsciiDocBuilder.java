package com.github.lucacampanella.callgraphflows.asciidoc;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.ClassDescriptionContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AsciiDocBuilder {
    private ClassDescriptionContainer classDescription;
    private ClassDescriptionContainer counterpartyClassDescription;

    public AsciiDocBuilder(ClassDescriptionContainer classDescription, ClassDescriptionContainer counterpartyClassDescription) {
        this.classDescription = classDescription;
        this.counterpartyClassDescription = counterpartyClassDescription;
    }

    public static AsciiDocBuilder fromAnalysisResult(AnalysisResult analysisResult) {
        if(!analysisResult.hasCounterpartyResult()) {
            return new AsciiDocBuilder(analysisResult.getClassDescription(), null);
        }
        final AnalysisResult counterpartyClassResult = analysisResult.getCounterpartyClassResult();
        return new AsciiDocBuilder(analysisResult.getClassDescription(), counterpartyClassResult.getClassDescription());
    }

    public void writeToFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("## ");
        sb.append(classDescription.getContainingClassNameOrItself());
        sb.append("\n");
        sb.append("Package: ");
        sb.append(classDescription.getPackageName());
        sb.append("\n\n");

        sb.append("*");
        sb.append(classDescription.getNameWithParent());
        sb.append("*");
        if(counterpartyClassDescription == null) {
            sb.append(" is a wrapper class, has no initiateFlow call, but calls other subflows. " +
                    "Thus this class has no direct counterparty class\n");
        }
        else {
            sb.append(" calls initiateFlow directly and thus has a counterparty class named *");
            sb.append(counterpartyClassDescription.getNameWithParent());
            sb.append("* ");
            if(!counterpartyClassDescription.getPackageName().equals(classDescription.getPackageName())) {
                sb.append("(From different package: ");
                sb.append(counterpartyClassDescription.getPackageName());
                sb.append(")");
            }
            sb.append("\n");
        }
        sb.append("\n");
        sb.append(classDescription.getComments());
        sb.append("\n");
        sb.append("image::");
        String filePath = "images/" + classDescription.getFullyQualifiedName() + ".svg";
        sb.append(filePath);
        sb.append("[link=\"");
        sb.append(filePath);
        sb.append("\"]\n");



        Files.write(Paths.get(path), sb.toString().getBytes());
    }
}
