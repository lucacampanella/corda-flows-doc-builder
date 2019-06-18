package com.github.lucacampanella.callgraphflows.graphics.svg.components;

public class GInstruction extends GBaseTextComponent {

    protected int lineNumber;

    public GInstruction(int lineNumber, String text) {
        super(text);
        this.lineNumber = lineNumber;
    }

    @Override
    public String getDisplayText() {
        return "[" + lineNumber + "] " + text;
    }

    @Override
    public String toString() {
            return getDisplayText();
    }

}
