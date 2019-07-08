package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.graphics.components2.GTwoSidedContainer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.InitiatingSubFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;

public class AnalysisResult {

    ClassDescriptionContainer classDescription;
    Branch statements = new Branch();
    AnalysisResult counterpartyClassResult = null;
    Boolean containsValidProtocolAndDrawn = null;
    private GTwoSidedContainer graphicRepresentation = null;

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

    public boolean checkIfContainsValidProtocolAndSetupLinks() {
        if(containsValidProtocolAndDrawn == null) {
            containsValidProtocolAndDrawn = checkIfContainsValidProtocolAndDrawNotLazy();
        }
        return containsValidProtocolAndDrawn;
    }

    private boolean checkIfContainsValidProtocolAndDrawNotLazy() {
        CombinationsHolder allCombinations = CombinationsHolder.fromBranch(statements);
        if (hasCounterpartyResult()) {
            CombinationsHolder counterpartyAllCombinations =
                    CombinationsHolder.fromBranch(counterpartyClassResult.getStatements());

            return allCombinations.checkIfMatchesAndDraw(counterpartyAllCombinations);
        } else {
            for (StatementInterface stmt : statements) {
                if (stmt.isSendOrReceive()) {
                    return false;
                }
                if (stmt instanceof InitiatingSubFlow &&
                        !((InitiatingSubFlow) stmt).checkIfContainsValidProtocolAndDraw()) {
                    return false;
                }
            }
            return true;
        }
    }

    public ClassDescriptionContainer getClassDescription() {
        return classDescription;
    }

    public GTwoSidedContainer getGraphicRepresentationNoTitles() {
        if(graphicRepresentation == null) {
            graphicRepresentation = GTwoSidedContainer.fromAnalysisResult(this);
        }
        return graphicRepresentation;
    }
}
