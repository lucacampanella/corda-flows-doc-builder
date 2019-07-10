package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class ContinueTestFlow {

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

                boolean condition = false;
                while(condition) {
                    int i = 0;
                    session.send(true);
                    subFlow(new SendTransactionFlow(session, list.get(i)));
                    if(condition) {
                        continue;
                    }
                    session.send(true);
                    i++;
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

                otherSession.receive(Boolean.class);
                subFlow(new ReceiveTransactionFlow(otherSession, true, StatesToRecord.ALL_VISIBLE));
                otherSession.receive(Boolean.class);

                otherSession.receive(Boolean.class);
                subFlow(new ReceiveTransactionFlow(otherSession, true, StatesToRecord.ALL_VISIBLE));
                //here the break is triggered

                otherSession.receive(String.class);
                return null;
            }
        }
    }
