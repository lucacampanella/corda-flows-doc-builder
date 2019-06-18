package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.graphics.svg.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.svg.components.GIfElse;
import com.github.lucacampanella.callgraphflows.graphics.svg.components.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import spoon.reflect.code.*;

public class IfElse extends BranchingStatement {

    GIfElse graphIfElse = new GIfElse();

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
    public GIfElse getGraphElem() {
        return toBePainted() ? graphIfElse : null;
    }

    @Override
    protected void buildGraphElem() {
        GIfElse elem = graphIfElse;

        elem.addBlock(getConditionInstruction(), getBranchTrue());
        treatIfElseCase(getBranchFalse(), graphIfElse);
    }

    private void treatIfElseCase(Branch falseBranch, GIfElse graphElem) {
        if(!falseBranch.isEmpty()) {
            final StatementInterface firstStatement = falseBranch.getStatements().get(0);
                if (falseBranch.getStatements().size() == 1 && firstStatement instanceof IfElse) {
                    IfElse ifElse = (IfElse) firstStatement;
                    final GInstruction conditionInstr = ifElse.getConditionInstruction();
                    conditionInstr.setText("else " + conditionInstr.getText());
                    graphElem.addBlock(conditionInstr, ifElse.getBranchTrue());
                    treatIfElseCase(ifElse.getBranchFalse(), graphElem);
                } else {
                    graphElem.addBlock(new GBaseTextComponent("else"), falseBranch);
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
