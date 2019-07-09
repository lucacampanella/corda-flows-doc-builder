package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseComponent;
import com.github.lucacampanella.callgraphflows.graphics.preferences.DefaultPreferences;
import com.github.lucacampanella.callgraphflows.graphics.preferences.PreferencesInterface;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.CombinationsHolder;

import java.util.Optional;

public interface StatementInterface {

    GBaseComponent getGraphElem();

    default boolean toBePainted() {
        return true;
    }

    default boolean needsCompanion() {
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
        getInternalMethodInvocations().forEach(stmt -> res.addIfRelevantForAnalysis(stmt.desugar()));
        res.add(this);
        return res;
    }

    /**
     * @return true if in the statement there is at least on interesting instruction in the analysis
     */
    default boolean isRelevantForAnalysis() {
        return true;
    }

    /**
     * @return true if it's an instruction relevant to be sisplayed and analyzed in a loop, such as break or continue
     */
    default boolean isRelevantForLoop() {
        return isRelevantForAnalysis();
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

    default String getStringDescription() {
           return this.toString();
    }

    /**
     * This method is overridden in {@link InitiatingSubFlow}, is called when checking the protocol makes
     * sense. Not to be confused with {@link StatementWithCompanionInterface#acceptCompanion(StatementWithCompanionInterface)}
     * which is called in a later stage.
     * @return true if the protocol makes sense, false otherwise
     */
    default boolean checkIfContainsValidProtocolAndSetupLinks() {
        return getInternalMethodInvocations().allInitiatingFlowsHaveValidProtocolAndSetupLinks();
    }

    /**
     * Returns all the possible combinations that can originate by this statement. By default returns the
     * statement itself, unless overridden for examble by {@link BranchingStatement}
     * @return all the possible combinations that can originate by this statement
     */
    default CombinationsHolder getResultingCombinations() {
        if(needsCompanion()) {
            return CombinationsHolder.fromSingleStatement(this);
        }
        return new CombinationsHolder(false);
    }

    default boolean hasSendOrReceiveAtThisLevel() {
        return getInternalMethodInvocations().hasSendOrReceiveAtThisLevel();
    }
}
