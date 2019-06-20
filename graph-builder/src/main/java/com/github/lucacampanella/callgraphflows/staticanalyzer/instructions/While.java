package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import spoon.reflect.code.*;

import java.util.ArrayList;
import java.util.List;

public class While extends BranchingStatement {
    private While() {
    }

    public static While fromStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        While whileInstr = new While();

        CtWhile whileStatement = (CtWhile) statement;

        whileInstr.initiateBlockingStatementAndConditionInstruction(whileStatement.getLoopingExpression(),
                statement, analyzer);

        //we unfold the loop only once for now
        whileInstr.branchTrue.add(MatcherHelper.fromCtStatementsToStatements(
                ((CtStatementList) whileStatement.getBody()).getStatements(), analyzer));

        if(whileInstr.hasBlockingStatementInCondition()){
            whileInstr.branchTrue.add(whileInstr.blockingStatementInCondition); //the while condition is checked once more when it's
            //false, so we append the condition again at the end to be able to reply to the other side if we entered
            //at least once in the while
        }

        whileInstr.branchFalse = new Branch(); //if the condition doesn't apply we directly go to the the next, so no
        //statements added in this branch

        whileInstr.buildGraphElem();

        return whileInstr;
    }

    protected While(CtStatement statement, CtExpression<Boolean> condition,
                 Branch bodyStatements, AnalyzerWithModel analyzer) {

        initiateBlockingStatementAndConditionInstruction(condition, statement, analyzer);

        //we unfold the loop only once for now
        branchTrue = new Branch();
        branchTrue.add(bodyStatements);
        branchFalse = new Branch(); //if the condition doesn't apply we directly go to the the next, so no
        //statements added in this branch

        if(hasBlockingStatementInCondition()){
            branchTrue.add(blockingStatementInCondition); //the while condition is checked once more when it's
            //false, so we append the condition again at the end to be able to reply to the other side if we entered
            //at least once in the while
        }

        graphElem = null;
    }

    @Override
    protected void buildGraphElem() {
        graphElem.setEnteringArrowText(getConditionInstruction());

        final List<StatementInterface> bodyStatements = new ArrayList<>(getBranchTrue().getStatements());
        if(hasBlockingStatementInCondition()) {
            bodyStatements.remove(bodyStatements.size()-1);
        }

        bodyStatements.forEach(stmt -> graphElem.addComponent(stmt.getGraphElem()));
    }

    @Override
    protected String formatDescription(CtStatement statement) {
        CtExpression condition;
        if(statement instanceof CtWhile) {
            condition = ((CtWhile) statement).getLoopingExpression();
        } else  {
            condition = ((CtDo) statement).getLoopingExpression();
        }
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
        sb.append(")");
        conditionDescription = sb.toString();
        conditionDescription = Utils.removeUnwrapIfWanted(condition, conditionDescription);
        return conditionDescription;
    }
}
