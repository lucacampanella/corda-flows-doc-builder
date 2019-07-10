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

        doWhile.conditionDescription = Utils.removePackageDescriptionIfWanted(
                whileStatement.getLoopingExpression().toString());
        doWhile.conditionLineNumber = statement.getPosition().getLine();

        doWhile.body.add(MatcherHelper.fromCtStatementsToStatements(
                        ((CtStatementList) whileStatement.getBody()).getStatements(), analyzer));


//        doWhile.branchTrue.add(doWhile.body);
//        doWhile.correspondingWhile =
//                new While(statement,  whileStatement.getLoopingExpression(), doWhile.body, analyzer);
//        doWhile.branchTrue.add(doWhile.correspondingWhile);

        //we create only one branch for the statements in the body, putting the branchFalse to null
        //we than add to the true branch in the end a while, containing again the body statements,
        //in this way we unfold the loop once and we basically desugar it to a while

//        doWhile.branchFalse = null;

        doWhile.conditionInstruction = new GInstruction(doWhile.conditionLineNumber, "do ");

        doWhile.buildGraphElem();

        return doWhile;
    }

    @Override
    protected void buildGraphElem() {
        GConditionalBranchIndented elem = graphElem;
        elem.setEnteringArrowText("do");
        getBody().getStatements().forEach(stmt -> elem.addComponent(stmt.getGraphElem()));
        elem.setExitingArrowText(getConditionInstruction());
    }

    @Override
    protected String formatDescription(CtStatement statement) {
        CtExpression condition = ((CtDo) statement).getLoopingExpression();
        String loopingExpression = condition.toString();
        conditionLineNumber = statement.getPosition().getLine();

        if(hasBlockingStatementInCondition()) {
            String blockingStatementCode =
                    MatcherHelper.getFirstMatchedStatementWithCompanion(condition).toString();
            loopingExpression = loopingExpression.replace(blockingStatementCode,
                    getBlockingStatementInCondition().getStringDescription());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("while(");
        sb.append(loopingExpression);
        sb.append(");");
        conditionDescription = sb.toString();
        conditionDescription = Utils.removeUnwrapIfWanted(condition, conditionDescription);
        return conditionDescription;
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
