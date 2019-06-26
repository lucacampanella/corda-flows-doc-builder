package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;

@InitiatingFlow
@StartableByRPC
public class ContainerFlow extends FlowLogic<Void> {

    private final Party otherParty;

    public ContainerFlow(Party otherParty) {
        this.otherParty = otherParty;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {

        subFlow(new DoWhileTestFlow.Initiator(otherParty));
        return null;
    }
}