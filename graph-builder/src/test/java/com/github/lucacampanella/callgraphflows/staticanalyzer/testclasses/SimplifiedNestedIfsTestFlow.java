package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class SimplifiedNestedIfsTestFlow {

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
                if(!list.isEmpty()) {
//                    session.send(true);

                    if(condition) {
                        session.send(true);
                    }
//                    else {
//                        session.send(false);
//                        boolean condition2 = true;
//                        if(condition) {
//                            session.receive(Boolean.class);
//                        }
//                        else if(condition2) {
//                            session.send("this is a string");
//                        }
//                        else {
//                            session.send(1);
//                        }
//                    }
                } else {
                    //fake flow, it cannot work if we go here
                    if(condition) {
                        session.send(false);
                    }// else {
//                        session.send(true);
//                    }
                    session.send("Fake");
                }
                session.send(false);
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
                if(condition) {
                    if(otherSession.receive(Boolean.class).unwrap(res -> res)) {
                        boolean condition2 = true;
                        if(condition) {
                            otherSession.send(true);
                        }
//                        else if(condition2) {
//                            int resInt = otherSession.receive(Integer.class).unwrap(intgr -> intgr);
//                        } else {
//                            String res = otherSession.receive(String.class).unwrap(str -> str);
//                        }
                    }
                }
                otherSession.receive(Boolean.class);
                return null;
            }
        }
    }
