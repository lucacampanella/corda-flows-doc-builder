package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.graphics.components.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import spoon.reflect.code.*;

public class DoWhile extends BranchingStatement {

    private Branch bodyStatements = new Branch();
    private While correspondingWhile;

    private DoWhile() {super();}

    public static DoWhile fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        DoWhile doWhile = new DoWhile();
        CtDo whileStatement = (CtDo) statement;

        doWhile.conditionDescription = Utils.removePackageDescriptionIfWanted(
                whileStatement.getLoopingExpression().toString());
        doWhile.conditionLineNumber = statement.getPosition().getLine();

        //we unfold the loop only once for now
        doWhile.bodyStatements.add(
                MatcherHelper.fromCtStatementsToStatements(
                        ((CtStatementList) whileStatement.getBody()).getStatements(), analyzer));


        doWhile.branchTrue.add(doWhile.bodyStatements);
        doWhile.correspondingWhile =
                new While(statement,  whileStatement.getLoopingExpression(), doWhile.bodyStatements, analyzer);
        doWhile.branchTrue.add(doWhile.correspondingWhile);

        //we create only one branch for the statements in the body, putting the branchFalse to null
        //we than add to the true branch in the end a while, containing again the body statements,
        //in this way we unfold the loop once and we basically desugar it to a while

        doWhile.branchFalse = null;

        doWhile.conditionInstruction = new GInstruction(doWhile.conditionLineNumber, "do ");

        doWhile.buildGraphElem();

        return doWhile;
    }

    @Override
    protected void buildGraphElem() {
        super.buildGraphElem();
        graphElem.addComponent(correspondingWhile.getConditionInstruction());
    }

    @Override
    protected String formatDescription(CtStatement statement) {
        return toString();
    }
}
