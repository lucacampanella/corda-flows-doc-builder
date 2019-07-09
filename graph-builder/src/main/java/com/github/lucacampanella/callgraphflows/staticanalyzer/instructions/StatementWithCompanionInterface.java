package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

public interface StatementWithCompanionInterface extends StatementInterface {

    boolean acceptCompanion(StatementWithCompanionInterface companion);

    void createGraphLink(StatementWithCompanionInterface companion);

    @Override
    default boolean needsCompanion() {
        return true;
    }

    /**
     * This method is mainly used to represent the sendAndReceive, which need two 'rounds' to be considered
     * consumed, since it accepts two companions, the send and the receive
     * @return true if the statement can be removed from the queue since it has accepted all companions it can,
     * false otherwise
     */
    default boolean isConsumedForCompanionAnalysis() {
        return true;
    }
}
