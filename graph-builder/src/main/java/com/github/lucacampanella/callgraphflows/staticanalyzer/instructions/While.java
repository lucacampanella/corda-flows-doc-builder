package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import spoon.reflect.code.*;

import java.util.ArrayList;
import java.util.List;

public class While extends LoopBranchingStatement {
    private While() {
    }

    public static While fromStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        While whileInstr = new While();

        CtWhile whileStatement = (CtWhile) statement;

        whileInstr.initiateBlockingStatementAndConditionInstruction(whileStatement.getLoopingExpression(),
                statement, analyzer);

        whileInstr.body.add(MatcherHelper.fromCtStatementsToStatements(
                ((CtStatementList) whileStatement.getBody()).getStatements(), analyzer));

        whileInstr.buildGraphElem();

        return whileInstr;
    }

    @Override
    protected String formatDescription(CtStatement statement) {
        return formatDescriptionFromCondition(((CtWhile) statement).getLoopingExpression());
    }
}
