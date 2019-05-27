package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;

public class AnalysisResult {

    String className;
    Branch statements = new Branch();
    AnalysisResult counterpartyClassResult = null;

    public AnalysisResult(String className) {
        this.className = className;
    }

    public AnalysisResult() {
        this.className = "No class";
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

    public String getClassName() {
        return className;
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
}
