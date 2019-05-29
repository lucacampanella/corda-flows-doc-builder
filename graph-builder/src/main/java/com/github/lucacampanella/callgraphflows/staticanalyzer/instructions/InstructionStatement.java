package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.graphics.components.GBaseGraphicComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import spoon.reflect.code.CtStatement;

import java.awt.*;
import java.util.Optional;

public class InstructionStatement implements StatementInterface {
    GInstruction graphElem;
    int line = 0;
    protected Optional<String> targetSessionName = Optional.empty();
    Branch internalMethodInvocations = new Branch();

    protected InstructionStatement(int line, String descr) {
        this.line = line;
        graphElem = new GInstruction(line, descr);
        graphElem.setBackgroundColor(getBackgroundColor());
        graphElem.setTextColor(getTextColor());
    }

    protected InstructionStatement(CtStatement statement) {
        this.line = statement.getPosition().getLine();
        graphElem = new GInstruction(line,
                addIconsToText(Utils.fromStatementToString(statement)));
        graphElem.setBackgroundColor(getBackgroundColor());
        graphElem.setTextColor(getTextColor());
    }

    protected InstructionStatement() {

    }

    @Override
    public GBaseGraphicComponent getGraphElem() {
        return toBePainted() ? graphElem : null;
    }

    @Override
    public Optional<String> getTargetSessionName() {
        return targetSessionName;
    }

    @Override
    public String toString() {
        return graphElem.toString();
    }

    @Override
    public Branch getInternalMethodInvocations() {
        return internalMethodInvocations;
    }

    protected Color getBackgroundColor() {
        return  Color.WHITE;
    }

    protected Color getTextColor() { return Color.BLACK; }

    protected int getLine() {
        return line;
    }

    protected void buildGraphElem() {
        graphElem = new GInstruction(getLine(),
                getStringDescription());
        graphElem.setBackgroundColor(getBackgroundColor());
        graphElem.setTextColor(getTextColor());
    }
}
