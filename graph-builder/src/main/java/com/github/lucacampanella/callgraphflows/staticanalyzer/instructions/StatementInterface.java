package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GBaseGraphicComponent;
import com.github.lucacampanella.callgraphflows.graphics.preferences.DefaultPreferences;
import com.github.lucacampanella.callgraphflows.graphics.preferences.PreferencesInterface;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;

import java.util.Optional;

public interface StatementInterface {

    GBaseGraphicComponent getGraphElem();

    default boolean toBePainted() {
        return true;
    }

    default boolean needsCompanion() {
        return false;
    }

    default boolean isBranchingStatement() {
        return false;
    }

    default boolean modifiesSession() {
        return false;
    }

    default boolean modifiesFlow() {
        return false;
    }

    default Optional<String> getTargetSessionName() {
        return Optional.empty();
    }

    //Implementation for statements that have just one instruction inside or don't need desugaring
    default Branch desugar() {
        Branch res = new Branch();
        getInternalMethodInvocations().forEach(stmt -> res.addIfRelevant(stmt.desugar()));
        res.add(this);
        return res;
    }

    default boolean isSendOrReceive() {
        return false;
    }

    default Branch flattenInternalMethodInvocations() {
        Branch res = new Branch();
        for (StatementInterface stmt : getInternalMethodInvocations()) {
            res.add(stmt.flattenInternalMethodInvocations());
            res.add(stmt);
        }
        return res;
    }

    /**
     * @return true if in the statement there is at least on interesting instruction in the analysis
     */
    default boolean isRelevantForAnalysis() {
        return true;
    }

    /**
     * @return an optional containing initiateFlow call if present at this level, meaning that it doesn't
     * look into subFlows
     */
    default Optional<InitiateFlow> getInitiateFlowStatementAtThisLevel() {
        return getInternalMethodInvocations().getInitiateFlowStatementAtThisLevel();
    }

    Branch getInternalMethodInvocations();

    default String addIconsToText(String displayText) {
        return displayText;
    }

    default PreferencesInterface getPref() {
        return DefaultPreferences.getInstance();
    }

    default String getStringDescription() {
           return this.toString();
    }
}
