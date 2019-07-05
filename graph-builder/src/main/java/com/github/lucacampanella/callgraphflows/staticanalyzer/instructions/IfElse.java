package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseText;
import com.github.lucacampanella.callgraphflows.graphics.components2.GIfElseIndented;
import com.github.lucacampanella.callgraphflows.graphics.components2.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import com.github.lucacampanella.callgraphflows.utils.Utils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;

public class IfElse extends BranchingStatement {

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
            ifElse.branchTrue.add(MatcherHelper.fromCtStatementsToStatementsForLoopBody(thenStatement.getStatements(), analyzer));
        }

        CtStatementList elseStatement = ifStatement.getElseStatement();
        ifElse.branchFalse = new Branch();
        if(elseStatement != null) { //there is an else statement
            ifElse.branchFalse.add(MatcherHelper.fromCtStatementsToStatementsForLoopBody(elseStatement.getStatements(), analyzer));
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

    @Override
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
        CtIf ifStatement = (CtIf) statement;
        conditionLineNumber = ifStatement.getPosition().getLine();

        final CtExpression<Boolean> condition = ifStatement.getCondition();

        String conditionExpression = condition.toString();

        if(hasBlockingStatementInCondition()) {
            String blockingStatementCode = MatcherHelper.getFirstMatchedStatementWithCompanion(condition).toString();
            conditionExpression = conditionExpression.replace(blockingStatementCode,
                    getBlockingStatementInCondition().getStringDescription());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("if(");
        sb.append(conditionExpression);
        sb.append(")");
        conditionDescription = sb.toString();
        conditionDescription = Utils.removeUnwrapIfWanted(condition, conditionDescription);
        return conditionDescription;
    }
}
