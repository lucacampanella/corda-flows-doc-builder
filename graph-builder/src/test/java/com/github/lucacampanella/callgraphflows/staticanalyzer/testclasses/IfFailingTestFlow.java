package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class IfFailingTestFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<Void> {

        private final Party otherParty;

        public Initiator(Party otherParty) {
            this.otherParty = otherParty;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {

            List<SignedTransaction> list = new LinkedList<>();

            FlowSession session = initiateFlow(otherParty);

            session.send(false);
            session.send(1);
            return null;
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Acceptor extends FlowLogic<Void> {

        private final FlowSession otherSession;

        public Acceptor(FlowSession otherSession) {
            this.otherSession = otherSession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {

            boolean condition = true;

            if(otherSession.receive(Boolean.class).unwrap(b -> b)) {
                otherSession.receive(String.class);
            } else {
                otherSession.receive(Float.class);
            }
            return null;
        }
    }
}
