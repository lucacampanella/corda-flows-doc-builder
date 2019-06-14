package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;

public class AnalysisResult {

    ClassDescriptionContainer classDescription;
    Branch statements = new Branch();
    AnalysisResult counterpartyClassResult = null;

    public AnalysisResult(ClassDescriptionContainer classDescription) {
        this.classDescription = classDescription;
    }

    public AnalysisResult() {
        this.classDescription = ClassDescriptionContainer.getEmpty();
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

    public ClassDescriptionContainer getClassDescription() {
        return classDescription;
    }
}
