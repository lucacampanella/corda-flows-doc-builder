package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public abstract class InitiatorBaseFlow extends FlowLogic<Void> {

    protected final Party otherParty;

    public InitiatorBaseFlow(Party otherParty) {
        this.otherParty = otherParty;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {


        FlowSession session = initiateFlow(otherParty);
        
        realCallMethod(session);
        
        session.send("END");
        return null;
    }

    @Suspendable
    protected abstract void realCallMethod(FlowSession session);
}