package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseText;
import com.github.lucacampanella.callgraphflows.graphics.components2.GConditionalBranchIndented;
import com.github.lucacampanella.callgraphflows.staticanalyzer.CombinationsHolder;
import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.graphics.components2.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.code.*;

import java.util.ArrayList;
import java.util.List;

public class DoWhile extends LoopBranchingStatement {

    private DoWhile() {super();}

    public static DoWhile fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        DoWhile doWhile = new DoWhile();
        CtDo whileStatement = (CtDo) statement;

        doWhile.initiateBlockingStatementAndConditionInstruction(whileStatement.getLoopingExpression(),
                statement, analyzer);

        doWhile.body.add(MatcherHelper.fromCtStatementsToStatements(
                        ((CtStatementList) whileStatement.getBody()).getStatements(), analyzer));

        Branch flattenedInternalMethodInvocations = new Branch();
        doWhile.getInternalMethodInvocations().forEach(stmt ->
                flattenedInternalMethodInvocations.addIfRelevantForLoopFlowBreakAnalysis(stmt.desugar()));
        doWhile.body.add(flattenedInternalMethodInvocations);

        doWhile.buildGraphElem();

        return doWhile;
    }

    @Override
    protected void buildGraphElem() {
        graphElem.setEnteringArrowText("do");
        getBody().getStatements().forEach(stmt -> graphElem.addComponent(stmt.getGraphElem()));
        graphElem.setExitingArrowText(getConditionInstruction());
    }

    @Override
    public Branch desugar() {
        return new Branch(this); //here we return only this object because all the internal method invocations
        //are already added to the while body
    }

    @Override
    protected String formatDescription(CtStatement statement) {
        return formatDescriptionFromCondition(((CtDo) statement).getLoopingExpression());
    }

    @Override
    public CombinationsHolder getResultingCombinations() {
        List<CombinationsHolder> unfoldedCombinations = new ArrayList<>(UNFOLD_ITERATIONS+1);
        CombinationsHolder bodyComb = CombinationsHolder.fromBranch(getBody());

        if(hasBlockingStatementInCondition()) {
            final CombinationsHolder combFromCondition = getBlockingStatementInCondition().getResultingCombinations();
            //to exit the loop we need to check the blocking condition
            bodyComb.combineWith(combFromCondition);
        }

        unfoldedCombinations.add(bodyComb); //this is the combination in case
        //the condition of the loop is false, at least once the body is executed

        return unfoldBody(unfoldedCombinations, bodyComb);
    }
}
