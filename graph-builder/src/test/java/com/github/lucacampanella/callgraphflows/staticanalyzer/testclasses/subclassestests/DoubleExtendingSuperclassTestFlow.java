package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests;

import co.paralleluniverse.fibers.Suspendable;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.ExtendingSuperclassTestFlow;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;

public class DoubleExtendingSuperclassTestFlow {

    public static class Initiator extends ExtendingSuperclassTestFlow.Initiator {

        public Initiator(Party otherParty) {
            super(otherParty);
        }

        @Suspendable
        @Override
        protected void realCallMethod(FlowSession session) {
            session.send("StrignPayload");
        }
    }

    @InitiatedBy(ExtendingSuperclassTestFlow.Initiator.class)
        public static class Acceptor extends ExtendingSuperclassTestFlow.Acceptor {

            public Acceptor(FlowSession otherSession) {
                super(otherSession);
            }
            @Suspendable
            @Override
            public Void call() throws FlowException {
                otherSession.send(1);
                otherSession.receive(String.class);
                SendTransactionFlow stf = new SendTransactionFlow(otherSession, null);
                return null;
            }

        }
}
