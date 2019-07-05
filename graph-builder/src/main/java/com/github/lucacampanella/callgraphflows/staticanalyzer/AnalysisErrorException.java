package com.github.lucacampanella.callgraphflows.staticanalyzer;

import spoon.reflect.declaration.CtClass;

public class AnalysisErrorException extends Exception {
    private final CtClass analyzedClass;
    private final String message;

    public AnalysisErrorException(CtClass analyzedClass, String msg) {
        this.analyzedClass = analyzedClass;
        this.message = msg;
    }

    //Message can be retrieved using this accessor method
    @Override
    public String getMessage() {
        return "[In analysis of class: " + analyzedClass.getQualifiedName() + "] " + message;
    }
}
