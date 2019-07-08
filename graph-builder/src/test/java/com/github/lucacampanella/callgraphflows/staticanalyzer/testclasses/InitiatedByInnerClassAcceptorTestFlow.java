package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;

@InitiatedBy(value= SimpleTestFlow.Initiator.class)
public class InitiatedByInnerClassAcceptorTestFlow extends FlowLogic<Void> {

    private final FlowSession otherSession;

    public InitiatedByInnerClassAcceptorTestFlow(FlowSession otherSession) {
        this.otherSession = otherSession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {

        boolean condition = true;

        otherSession.receive(Boolean.class);
        otherSession.send("Test");
        return null;
    }
}