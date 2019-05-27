package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;

public class ExtendingSuperclassTest {

        @InitiatingFlow
        @StartableByRPC
        public static class Initiator extends InitiatorBase {

            public Initiator(Party otherParty) {
                super(otherParty);
            }

            @Suspendable
            @Override
            protected void realCallMethod(FlowSession session) {
                session.send(1);
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
                otherSession.receive(Integer.class);
                otherSession.receive(String.class);
                return null;
            }
        }
    }
