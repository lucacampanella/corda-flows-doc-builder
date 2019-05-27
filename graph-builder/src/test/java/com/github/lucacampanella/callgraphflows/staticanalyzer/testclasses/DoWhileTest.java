package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class DoWhileTest {

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

                if(!list.isEmpty()) {
                    int i = 0;
                    do {
                        session.send(true);
                        subFlow(new SendTransactionFlow(session, list.get(i)));
                        i++;
                    } while (i < list.size());
                }
                session.send(false);
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

                while (otherSession.receive(Boolean.class).unwrap(data -> data)) {
                    subFlow(new ReceiveTransactionFlow(otherSession, true, StatesToRecord.ALL_VISIBLE));
                    // TODO 10-Apr-2019/mvk: make sure this is linked to the car
                }
                otherSession.receive(String.class);
                return null;
            }
        }
    }
