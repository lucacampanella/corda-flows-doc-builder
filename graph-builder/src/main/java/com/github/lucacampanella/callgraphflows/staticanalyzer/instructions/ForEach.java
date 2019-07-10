package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import spoon.reflect.code.*;

import java.util.ArrayList;
import java.util.List;

public class ForEach extends LoopBranchingStatement {

    String stringRepresentation;

    private ForEach() {
        super();
    }

    public static ForEach fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {

        ForEach forInstr = new ForEach();

        CtForEach forStatement = (CtForEach) statement;

        //we unfold the loop only once for now
        forInstr.body.add(MatcherHelper.fromCtStatementsToStatements(
                        ((CtStatementList) forStatement.getBody()).getStatements(), analyzer));

        CtExpression<?> condition = forStatement.getExpression();

        StringBuilder sb = new StringBuilder();
        sb.append("for(");
        sb.append(forStatement.getVariable().toString());
               sb.append(" : ");
        sb.append(condition.toString());
        sb.append(")");

        forInstr.initiateBlockingStatementAndConditionInstruction(condition, statement, analyzer);

        forInstr.buildGraphElem();

        return forInstr;
    }

    @Override
    protected String formatDescription(CtStatement statement) {
        CtForEach forStatement = (CtForEach) statement;

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
