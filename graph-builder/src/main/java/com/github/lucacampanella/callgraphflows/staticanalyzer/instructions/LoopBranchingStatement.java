package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GConditionalBranchIndented;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.CombinationsHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
    public boolean isRelevantForAnalysis() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.isRelevantForAnalysis()) {
            return true;
        }
        return internalMethodInvocations.isRelevant() || getBody().isRelevant();
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
            unfoldedCombinations.add(curr);
        }

        CombinationsHolder res = new CombinationsHolder(false);
        unfoldedCombinations.forEach(comb -> res.mergeWith(comb));

        return res;
    }
}
