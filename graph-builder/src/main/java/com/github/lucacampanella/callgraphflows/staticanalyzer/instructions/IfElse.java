package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseText;
import com.github.lucacampanella.callgraphflows.graphics.components2.GIfElseIndented;
import com.github.lucacampanella.callgraphflows.graphics.components2.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.CombinationsHolder;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;

import java.util.Optional;

public class IfElse extends BranchingStatement {

    protected Branch branchTrue = new Branch();
    protected Branch branchFalse = new Branch();

    GIfElseIndented graphIfElse = new GIfElseIndented();

    private IfElse() {
        super();
    }

    public static IfElse fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        IfElse ifElse = new IfElse();

        CtIf ifStatement = (CtIf) statement;


        CtStatementList thenStatement = ifStatement.getThenStatement();
        ifElse.branchTrue = new Branch();
        if(thenStatement != null) { //there is a then statement
            ifElse.branchTrue.add(MatcherHelper.fromCtStatementsToStatements(thenStatement.getStatements(), analyzer));
        }

        CtStatementList elseStatement = ifStatement.getElseStatement();
        ifElse.branchFalse = new Branch();
        if(elseStatement != null) { //there is an else statement
            ifElse.branchFalse.add(MatcherHelper.fromCtStatementsToStatements(elseStatement.getStatements(), analyzer));
        }
        //here the statements can be also if statements if an "else if" condition is applied

        final CtExpression<Boolean> condition = ifStatement.getCondition();

        ifElse.initiateBlockingStatementAndConditionInstruction(condition, statement, analyzer);

        ifElse.buildGraphElem();

        return ifElse;
    }

    @Override
    public GIfElseIndented getGraphElem() {
        return toBePainted() ? graphIfElse : null;
    }

    protected void buildGraphElem() {
        graphIfElse.addBlock(getConditionInstruction(), getBranchTrue());
        treatIfElseCase(getBranchFalse(), graphIfElse);
    }

    private void treatIfElseCase(Branch falseBranch, GIfElseIndented graphElem) {
        if(!falseBranch.isEmpty()) {
            final StatementInterface firstStatement = falseBranch.getStatements().get(0);
                if (falseBranch.getStatements().size() == 1 && firstStatement instanceof IfElse) {
                    IfElse ifElse = (IfElse) firstStatement;
                    final GInstruction conditionInstr = ifElse.getConditionInstruction();
                    conditionInstr.setText("else " + conditionInstr.getText());
                    graphElem.addBlock(conditionInstr, ifElse.getBranchTrue());
                    treatIfElseCase(ifElse.getBranchFalse(), graphElem);
                } else {
                    graphElem.addBlock(new GBaseText("else"), falseBranch);
                }
            }
        }

    @Override
    protected String formatDescription(CtStatement statement) {
        return formatDescriptionFromCondition(((CtIf) statement).getCondition());
    }

    @Override
    public boolean isRelevantForLoopFlowBreakAnalysis() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.isRelevantForLoopFlowBreakAnalysis()) {
            return true;
        }
        return getBranchTrue().isRelevantForLoopFlowBreakAnalysis() || getBranchFalse().isRelevantForLoopFlowBreakAnalysis();
    }

    @Override
    public boolean isRelevantForMethodFlowBreakAnalysis() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.isRelevantForMethodFlowBreakAnalysis()) {
            return true;
        }
        return getBranchTrue().isRelevantForMethodFlowBreakAnalysis() || getBranchFalse().isRelevantForMethodFlowBreakAnalysis();
    }

    @Override
    public boolean isRelevantForProtocolAnalysis() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.isRelevantForProtocolAnalysis()) {
            return true;
        }
        return getBranchTrue().isRelevantForProtocolAnalysis() || getBranchFalse().isRelevantForProtocolAnalysis();
    }

    @Override
    public boolean toBePainted() {
        if(hasBlockingStatementInCondition() && blockingStatementInCondition.toBePainted()) {
            return true;
        }
        return getBranchTrue().toBePainted() || getBranchFalse().toBePainted();
    }

    /**
     * @return true if there is an initiateFlow call, but doesn't look into subFlows
     */
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
        res = getBranchTrue().getInitiateFlowStatementAtThisLevel();
        if(res.isPresent()) {
            return res;
        }
        res = getBranchFalse().getInitiateFlowStatementAtThisLevel();
        return res;
    }

    @Override
    public CombinationsHolder getResultingCombinations() {
        CombinationsHolder mergedCombination = new CombinationsHolder(false);
        if(getBranchTrue() != null) {
            mergedCombination.mergeWith(CombinationsHolder.fromBranch(getBranchTrue()));
        }
        if(getBranchFalse() != null) {
            mergedCombination.mergeWith(CombinationsHolder.fromBranch(getBranchFalse()));
        }
        CombinationsHolder res;
        if(hasBlockingStatementInCondition()) {
            res = getBlockingStatementInCondition().getResultingCombinations();
            res.combineWith(mergedCombination);
        }
        else {
            res = mergedCombination;
        }
        return res;
    }

    public Branch getBranchTrue() {
        return branchTrue;
    }
    public Branch getBranchFalse() {
        return branchFalse;
    }

    @Override
    public boolean checkIfContainsValidProtocolAndSetupLinks() {
        return super.checkIfContainsValidProtocolAndSetupLinks()
                && (!hasBlockingStatementInCondition()
                || getBlockingStatementInCondition().checkIfContainsValidProtocolAndSetupLinks()) &&
                getBranchTrue().allInitiatingFlowsHaveValidProtocolAndSetupLinks() &&
                getBranchFalse().allInitiatingFlowsHaveValidProtocolAndSetupLinks();
    }

    @Override
    public boolean hasSendOrReceiveAtThisLevel() {
        return super.hasSendOrReceiveAtThisLevel() ||
                (hasBlockingStatementInCondition() && getBlockingStatementInCondition().hasSendOrReceiveAtThisLevel())
                || getBranchTrue().hasSendOrReceiveAtThisLevel() ||
                getBranchFalse().hasSendOrReceiveAtThisLevel();
    }
}
