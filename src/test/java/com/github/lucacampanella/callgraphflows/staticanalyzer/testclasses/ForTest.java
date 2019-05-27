package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class ForTest {

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

                for (int i = 0, testInt = session.receive(Integer.class).unwrap(res -> res); i < list.size(); ++i) {
                    session.send(true);
                    subFlow(new SendTransactionFlow(session, list.get(i)));
                }
                session.send(false);

                for (int i = 0; i < session.receive(Integer.class).unwrap(res -> res); ++i) {
                    subFlow(new SendTransactionFlow(session, list.get(i)));
                }
                session.send("END");
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
                otherSession.send(1);
                while (otherSession.receive(Boolean.class).unwrap(data -> data)) {
                    subFlow(new ReceiveTransactionFlow(otherSession, true, StatesToRecord.ALL_VISIBLE));
                }
                int i = 0;
                while (i < 5) {
                    i++;
                    otherSession.send(i);
                    subFlow(new ReceiveTransactionFlow(otherSession, true, StatesToRecord.ALL_VISIBLE));
                }
                otherSession.send(i);
                otherSession.receive(String.class);
                return null;
            }
        }
    }
