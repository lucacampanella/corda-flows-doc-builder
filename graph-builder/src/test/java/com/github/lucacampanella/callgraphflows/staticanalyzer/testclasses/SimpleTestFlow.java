package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class SimpleTestFlow {

    /**
     * These are javadoc comments that will appear in the asciidoc file and should describe the
     * behaviour of the class
     */
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

            session.sendAndReceive(String.class, false);
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

            otherSession.receive(Boolean.class);
            otherSession.send("Test");
            return null;
        }
    }
}
