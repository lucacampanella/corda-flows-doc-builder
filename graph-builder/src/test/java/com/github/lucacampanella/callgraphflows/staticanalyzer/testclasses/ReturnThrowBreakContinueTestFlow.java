package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class ReturnThrowBreakContinueTestFlow {

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

                methodWithRelevantStuff(session, list);
                session.send("END");

                return null;
            }

            private void methodWithRelevantStuff(FlowSession session, List<SignedTransaction> list) throws FlowException {
                boolean condition = false;
                boolean condition2 = false;
                boolean condition3 = false;
                while(condition) {
                    int i = 0;
                    session.send(true);
                    subFlow(new SendTransactionFlow(session, list.get(i)));
                    if(condition && condition && condition3) {
                        continue;
                    }
                    else if(condition && condition2) {
                        return;
                    }
                    else if(condition) {
                        break;
                    }

                    while(condition) {
                        int j = 0;
                        session.send(true);
                        subFlow(new SendTransactionFlow(session, list.get(i)));
                        if(condition && condition && condition3) {
                            continue;
                        }
                        else if(condition && condition2) {
                            return;
                        }
                        else if(condition) {
                            break;
                        }

                        session.send(true);
                        subFlow(new SendTransactionFlow(session, list.get(i)));

                        j++;
                    }

                    i++;
                }
                session.send(false);

                if(condition) {
                    return;
                }
                session.send(3); //nowhere in the other session there is a receive(Integer.class)
                return;
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
                }
                otherSession.receive(String.class);
                return null;
            }
        }
    }
