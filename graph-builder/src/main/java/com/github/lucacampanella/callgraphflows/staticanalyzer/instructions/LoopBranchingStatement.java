package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GConditionalBranchIndented;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.CombinationsHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class LoopBranchingStatement extends BranchingStatement {
    protected static final int UNFOLD_ITERATIONS = 2;

    GConditionalBranchIndented graphElem = new GConditionalBranchIndented();
    Branch body = new Branch();

    @Override
    public GConditionalBranchIndented getGraphElem() {
        return toBePainted() ? graphElem : null;
    }

    protected void buildGraphElem() {
        GConditionalBranchIndented elem = graphElem;

        elem.setEnteringArrowText(getConditionInstruction());
        getBody().getStatements().forEach(stmt -> elem.addComponent(stmt.getGraphElem()));
    }

    @Override
    public boolean isRelevantForLoopFlowBreakAnalysis() {
        return isRelevantForMethodFlowBreakAnalysis();
    }

    @Override
    public boolean isRelevantForMethodFlowBreakAnalysis() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.isRelevantForMethodFlowBreakAnalysis()) {
            return true;
        }
        return getBody().isRelevantForMethodFlowBreakAnalysis();
    }

    @Override
    public boolean isRelevantForProtocolAnalysis() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.isRelevantForProtocolAnalysis()) {
            return true;
        }
        return getBody().isRelevantForProtocolAnalysis();
    }

    @Override
    public boolean toBePainted() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.toBePainted()) {
            return true;
        }
        return getBody().toBePainted();
    }

    public Branch getBody() {
        return body;
    }

    @Override
    public CombinationsHolder getResultingCombinations() {
        List<CombinationsHolder> unfoldedCombinations = new ArrayList<>(UNFOLD_ITERATIONS+1);
        CombinationsHolder bodyComb = CombinationsHolder.fromBranch(getBody());

        unfoldedCombinations.add(new CombinationsHolder(true)); //this is the combination in case
        //the condition of the loop is false, just an empty combination

        if(hasBlockingStatementInCondition()) {
            final CombinationsHolder combFromCondition = getBlockingStatementInCondition().getResultingCombinations();
            //to enter the loop we need to check the blocking statement
            unfoldedCombinations.get(0).combineWith(combFromCondition);

            //to exit the loop we need to check the blocking condition
            bodyComb.combineWith(combFromCondition);
        }
        return unfoldBody(unfoldedCombinations, bodyComb);
    }

    @NotNull
    static CombinationsHolder unfoldBody(List<CombinationsHolder> unfoldedCombinations, CombinationsHolder bodyComb) {
        for(int i = 1; i < UNFOLD_ITERATIONS + 1; ++i) {
            CombinationsHolder curr = CombinationsHolder.fromOtherCombination(unfoldedCombinations.get(i-1));
            curr.combineWith(bodyComb);
            curr.removeAllContinueLoopLocks();
            unfoldedCombinations.add(curr);
        }

        CombinationsHolder res = new CombinationsHolder(false);
        unfoldedCombinations.forEach(comb -> res.mergeWith(comb));

        res.removeAllLoopLocks();

        return res;
    }

    @Override
    public boolean checkIfContainsValidProtocolAndSetupLinks() {
        return super.checkIfContainsValidProtocolAndSetupLinks()
                && (!hasBlockingStatementInCondition()
                || getBlockingStatementInCondition().checkIfContainsValidProtocolAndSetupLinks()) &&
                getBody().allInitiatingFlowsHaveValidProtocolAndSetupLinks();
    }

    @Override
    public Optional<InitiateFlow> getInitiateFlowStatementAtThisLevel() {
        Optional<InitiateFlow>
                res = super.getInitiateFlowStatementAtThisLevel();
        if(res.isPresent()) {
            return res;
        }
        if(hasBlockingStatementInCondition()) {
            res = getBlockingStatementInCondition().getInitiateFlowStatementAtThisLevel();
            if(res.isPresent()) {
                return res;
            }
        }
        res = getBody().getInitiateFlowStatementAtThisLevel();
        return res;
    }

    @Override
    public boolean hasSendOrReceiveAtThisLevel() {
        return super.hasSendOrReceiveAtThisLevel() ||
                (hasBlockingStatementInCondition() && getBlockingStatementInCondition().hasSendOrReceiveAtThisLevel())
                || getBody().hasSendOrReceiveAtThisLevel();
    }
}
