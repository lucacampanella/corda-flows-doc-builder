package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;

public class AnalysisResult {

    String classSimpleName;
    String classFullyQualifiedName;
    String classCommentForDocumentation;
    Branch statements = new Branch();
    AnalysisResult counterpartyClassResult = null;

    public AnalysisResult(String classSimpleName, String classFullyQualifiedName) {
        this.classSimpleName = classSimpleName;
        this.classFullyQualifiedName = classFullyQualifiedName;
    }

    public AnalysisResult(String classSimpleName) {
        this.classSimpleName = classSimpleName;
    }

    public AnalysisResult() {
        this.classSimpleName = "No class";
    }

    public Branch getStatements() {
        return statements;
    }

    public AnalysisResult getCounterpartyClassResult() {
        return counterpartyClassResult;
    }

    public boolean hasCounterpartyResult() {
        return  counterpartyClassResult != null;
    }

    public void setCounterpartyClassResult(AnalysisResult counterpartyClassResult) {
        this.counterpartyClassResult = counterpartyClassResult;
    }

    public void setStatements(Branch statements) {
        this.statements = statements;
    }

    public String getClassSimpleName() {
        return classSimpleName;
    }

    public boolean containsValidProtocol() {
        CombinationsHolder allCombinations = CombinationsHolder.fromBranch(statements);
        if(hasCounterpartyResult()) {
            CombinationsHolder counterpartyAllCombinations =
                    CombinationsHolder.fromBranch(counterpartyClassResult.getStatements());

            return allCombinations.hasOneMatchWith(allCombinations);
        }
        else {
            for(StatementInterface stmt : statements) {
                if(stmt.isSendOrReceive()) {
                    return false;
                }
                //todo
            }
        }
        return false;
    }

    public void setClassCommentForDocumentation(String classCommentForDocumentation) {
        this.classCommentForDocumentation = classCommentForDocumentation;
    }

    public String getClassCommentForDocumentation() {
        return classCommentForDocumentation;
    }

    public String getClassFullyQualifiedName() {
        return classFullyQualifiedName;
    }
}
