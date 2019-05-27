package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzer;
import spoon.reflect.code.CtStatement;

import java.awt.*;

public class TransactionBuilder extends InstructionStatement {

    protected TransactionBuilder(CtStatement statement) {
        super(statement);
    }

    public static TransactionBuilder fromStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        TransactionBuilder transactionBuilder = new TransactionBuilder(statement);
        transactionBuilder.internalMethodInvocations.add(
                StaticAnalyzer.getAllRelevantMethodInvocations(statement, analyzer));

        return transactionBuilder;
    }

    protected Color getTextColor() { return GBaseTextComponent.LESS_IMPORTANT_TEXT_COLOR; }

    @Override
    public boolean toBePainted() {
        return false;
    }
}
