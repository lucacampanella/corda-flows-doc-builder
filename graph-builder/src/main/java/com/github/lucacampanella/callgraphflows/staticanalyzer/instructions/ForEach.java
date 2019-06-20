package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import spoon.reflect.code.*;

public class ForEach extends BranchingStatement {

    String stringRepresentation;

    private ForEach() {
        super();
    }

    public static ForEach fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {

        ForEach forInstr = new ForEach();

        CtForEach forStatement = (CtForEach) statement;

        //we unfold the loop only once for now
        forInstr.branchTrue.add(
                MatcherHelper.fromCtStatementsToStatementsForLoopBody(
                        ((CtStatementList) forStatement.getBody()).getStatements(), analyzer));

        CtExpression<?> condition = forStatement.getExpression();

        StringBuilder sb = new StringBuilder();
        sb.append("for(");
        sb.append(forStatement.getVariable().toString());
               sb.append(" : ");
        sb.append(condition.toString());
        sb.append(")");

        forInstr.initiateBlockingStatementAndConditionInstruction(condition, statement, analyzer);

        forInstr.branchFalse = new Branch(); //if the condition doesn't apply we directly go to the the next, so no
        //statements added in this branch

        forInstr.buildGraphElem();

        return forInstr;
    }

    @Override
    protected String formatDescription(CtStatement statement) {
        CtForEach forStatement = (CtForEach) statement;
        conditionLineNumber = statement.getPosition().getLine();

        final CtExpression<?> condition = forStatement.getExpression();
        String loopingExpression = condition.toString();

        if(hasBlockingStatementInCondition()) {
            String blockingStatementCode = MatcherHelper.getFirstMatchedStatementWithCompanion(condition).toString();
            loopingExpression = loopingExpression.replace(blockingStatementCode,
                    getBlockingStatementInCondition().getStringDescription());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("for(");
        sb.append(forStatement.getVariable().toString());
        sb.append(" : ");
        sb.append(loopingExpression);
        sb.append(")");
        conditionDescription = sb.toString();
        conditionDescription = Utils.removeUnwrapIfWanted(condition, conditionDescription);
        return conditionDescription;
    }

}
