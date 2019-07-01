package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;

public class ExtendingSuperclassTestFlow {

        @StartableByRPC
        public static class Initiator<T> extends InitiatorBaseFlow<T> {
            T returnValue;

            public Initiator(Party otherParty) {
                super(otherParty);
            }

            @Suspendable
            @Override
            protected T realCallMethod(FlowSession session) {
                session.send(1);
                return returnValue;
            }
        }

        @InitiatedBy(DoubleExtendingSuperclassTestFlow.Initiator.class)
        public static class Acceptor extends FlowLogic<Void> {

            protected final FlowSession otherSession;

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
