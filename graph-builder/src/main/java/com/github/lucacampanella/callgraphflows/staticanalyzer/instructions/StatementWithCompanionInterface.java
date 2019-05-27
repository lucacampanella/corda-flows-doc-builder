package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

public interface StatementWithCompanionInterface extends StatementInterface {

    boolean acceptCompanion(StatementWithCompanionInterface companion);

    void createGraphLink(StatementWithCompanionInterface companion);

    @Override
    default boolean needsCompanion() {
        return true;
    }
}
