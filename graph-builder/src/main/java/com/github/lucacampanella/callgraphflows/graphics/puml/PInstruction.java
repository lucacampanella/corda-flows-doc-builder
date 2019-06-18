package com.github.lucacampanella.callgraphflows.graphics.puml;

import com.github.lucacampanella.callgraphflows.graphics.svg.components.GBaseTextComponent;

public class PInstruction extends PBaseComponent {

    protected int lineNumber;
    protected String text;

    public PInstruction(int lineNumber, String text) {
        this.text = text;
        this.lineNumber = lineNumber;
    }

    @Override
    public String getPUMLString() {
        return "[" + lineNumber + "] " + text;
    }
}
