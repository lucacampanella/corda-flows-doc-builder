package com.github.lucacampanella.callgraphflows.graphics.components2;

public class GInstruction extends GBaseText {
    private static boolean drawLineNumbers = false;

    protected int lineNumber;

    public GInstruction(int lineNumber, String text) {
        super(text);
        this.lineNumber = lineNumber;
    }

    @Override
    public String getDisplayText() {
        if(drawLineNumbers) {
            return "[" + lineNumber + "] " + text;
        }
        return text;
    }

    @Override
    public String toString() {
        return getDisplayText();
    }

    public static void setDrawLineNumbers(boolean drawLineNumbers) {
        GInstruction.drawLineNumbers = drawLineNumbers;
    }

}
