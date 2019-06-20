package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import spoon.reflect.code.*;

import java.util.List;
import java.util.stream.Collectors;

public class For extends BranchingStatement {
    private static final String TO_BE_REPLACED_PATTERN = "1@#45^";

    Branch initBranch = new Branch();

    private For() {
        super();
    }

    public static For fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {

        For forInstr = new For();

        CtFor forStatement = (CtFor) statement;

        final CtExpression<Boolean> condition = forStatement.getExpression();

        List<CtStatement> init = forStatement.getForInit();
        Branch initBlockingStatements = MatcherHelper.fromCtStatementsToStatements(init, analyzer);

        if(!initBlockingStatements.isEmpty()) { //if there are some blocking statements in the init block of the for
            //we just push them before the for
            init = init.stream().filter(stmt -> MatcherHelper.instantiateStatement(stmt, analyzer) == null)
                    .collect(Collectors.toList());
            statement = statement.clone();
            forStatement = (CtFor) statement;
            forStatement.setForInit(init);

            forInstr.initBranch.add(initBlockingStatements);
        }

        //we unfold the loop only once for now
        forInstr.branchTrue.add(
                MatcherHelper.fromCtStatementsToStatementsForLoopBody(
                ((CtStatementList) forStatement.getBody()).getStatements(), analyzer));

        List<CtStatement> update = forStatement.getForUpdate();

        Branch updateBlockingStatements = MatcherHelper.fromCtStatementsToStatements(update, analyzer);

        if(!updateBlockingStatements.isEmpty()) { //if there are some blocking statements in the update block of the for
            //we just push them at the end of the true block part
            update = update.stream().filter(stmt -> MatcherHelper.instantiateStatement(stmt, analyzer) == null)
                    .collect(Collectors.toList());
            statement = statement.clone();
            forStatement = (CtFor) statement;
            forStatement.setForUpdate(update);

            forInstr.branchTrue.add(updateBlockingStatements);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("for(");
        String initString =  init.stream().map(Object::toString)
                .collect(Collectors.joining(", "));
        sb.append(initString);
        sb.append("; ");
        sb.append(TO_BE_REPLACED_PATTERN);
        sb.append("; ");
        String updateString =  update.stream().map(Object::toString)
                .collect(Collectors.joining(", "));
        sb.append(updateString);
        sb.append(")");

        forInstr.conditionDescription = sb.toString();

        forInstr.initiateBlockingStatementAndConditionInstruction(condition, statement, analyzer);

        forInstr.buildGraphElem();

        return forInstr;
    }

    @Override
    public Branch desugar() { //we desugar the for in case it has a receive call in the init statements
        Branch result = new Branch();
        result.addIfRelevantForAnalysis(getInternalMethodInvocations());
        result.add(initBranch);
        result.add(this);

        return result;
    }

    @Override
    protected String formatDescription(CtStatement statement) {
        CtFor forStatement = (CtFor) statement;
        conditionLineNumber = statement.getPosition().getLine();

        final CtExpression<Boolean> condition = forStatement.getExpression();
        String loopingExpression = condition.toString();

        if(hasBlockingStatementInCondition()) {
            String blockingStatementCode = MatcherHelper.getFirstMatchedStatementWithCompanion(condition).toString();
            loopingExpression = loopingExpression.replace(blockingStatementCode,
                    getBlockingStatementInCondition().getStringDescription());
        }
        conditionDescription = conditionDescription.replace(TO_BE_REPLACED_PATTERN, loopingExpression);
        conditionDescription = Utils.removeUnwrapIfWanted(condition, conditionDescription);
        return conditionDescription;
    }
}
