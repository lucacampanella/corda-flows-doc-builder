package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseContainer;
import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseText;
import com.github.lucacampanella.callgraphflows.graphics.components2.GConditionalBranchIndented;
import com.github.lucacampanella.callgraphflows.staticanalyzer.CombinationsHolder;
import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.graphics.components2.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;

import java.util.Optional;

/**
 * Father class for all statements in which a branching can happen, like {@link For}, {@link ForEach}, {@link IfElse}
 * etc...
 */
public abstract class BranchingStatement implements StatementWithCompanionInterface {

    protected String conditionDescription;
    protected int conditionLineNumber = -1;
    StatementWithCompanionInterface blockingStatementInCondition;
    GInstruction conditionInstruction = null;
    Branch internalMethodInvocations = new Branch();


    protected BranchingStatement(){}

    public boolean hasBlockingStatementInCondition() {
        return blockingStatementInCondition != null;
    }

    public StatementWithCompanionInterface getBlockingStatementInCondition() {
        return blockingStatementInCondition;
    }

    @Override
    public String toString() {
        return "BranchingStatement (body omitted): " + getStringRepresentation();
    }

    public boolean acceptCompanion(StatementWithCompanionInterface companion) {
        if(!hasBlockingStatementInCondition()) {
            throw new IllegalStateException("acceptCompanion called on a branch instruction that" +
                    " doesn't have a blocking statement in the condition");
        }
        return getBlockingStatementInCondition().acceptCompanion(companion);
    }

    @Override
    public void createGraphLink(StatementWithCompanionInterface companion) {
        if(!hasBlockingStatementInCondition()) {
            throw new IllegalStateException("createGraphLink called on a branch instruction that" +
                    " doesn't have a blocking statement in the condition");
        }
        getBlockingStatementInCondition().createGraphLink(companion);
    }

    @Override
    public boolean needsCompanion() {
        return hasBlockingStatementInCondition();
    }

    @Override
    public boolean isConsumedForCompanionAnalysis() {
        return !hasBlockingStatementInCondition() || blockingStatementInCondition.isConsumedForCompanionAnalysis();
    }

    public String getStringRepresentation() {
        return conditionDescription;
    }

    protected GInstruction getConditionInstruction() {
        return hasBlockingStatementInCondition() ? (GInstruction) getBlockingStatementInCondition().getGraphElem() :
                conditionInstruction;
    }

    @Override
    public Optional<String> getTargetSessionName() {
        if(hasBlockingStatementInCondition()) {
            return blockingStatementInCondition.getTargetSessionName();
        }
        return Optional.empty();
    }

    protected void initiateBlockingStatementAndConditionInstruction(CtExpression condition,
                                                                           CtStatement statement,
                                                                           AnalyzerWithModel analyzer) {
        this.blockingStatementInCondition = null;
        //automatically initiated to null if the condition doesn't contain a matching blocking statement that
        //communicates with the other flow
        final StatementInterface blockingStatementInConditionWithSubflow =
                MatcherHelper.instantiateStatementIfQueryableMatches(condition, statement, analyzer);
        //todo: what if there are two blocking statements in the loop condition?

        if(blockingStatementInConditionWithSubflow instanceof InlinableSubFlow
        || blockingStatementInConditionWithSubflow instanceof InitiatingSubFlow) {
            internalMethodInvocations.add(blockingStatementInConditionWithSubflow);
        }
        else {
            blockingStatementInCondition = (StatementWithCompanionInterface)
                    blockingStatementInConditionWithSubflow;
        }

        conditionLineNumber = statement.getPosition().getLine();
        formatDescription(statement);
        conditionDescription = Utils.removePackageDescriptionIfWanted(conditionDescription);

        if(this.hasBlockingStatementInCondition()) {
            this.conditionInstruction =
                    (GInstruction) this.getBlockingStatementInCondition().getGraphElem();
            this.conditionInstruction.setText(conditionDescription);
        }
        else {
            this.conditionInstruction =
                    new GInstruction(this.conditionLineNumber, this.conditionDescription);
            this.conditionInstruction.setTextColor(GBaseText.LESS_IMPORTANT_TEXT_COLOR);
        }

        this.internalMethodInvocations.add(
                StaticAnalyzerUtils.getAllRelevantMethodInvocations(condition, analyzer));
    }

    protected abstract String formatDescription(CtStatement statement);

    @Override
    public Branch getInternalMethodInvocations() {
        return internalMethodInvocations;
    }

    protected String formatDescriptionFromCondition(CtExpression condition) {
        String loopingExpression = condition.toString();

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
