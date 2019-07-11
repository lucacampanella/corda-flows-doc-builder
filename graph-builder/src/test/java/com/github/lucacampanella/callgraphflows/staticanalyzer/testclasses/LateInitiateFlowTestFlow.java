package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class LateInitiateFlowTestFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<Void> {

        private final Party otherParty;
        private final FlowSession session;

        public Initiator(Party otherParty, FlowSession session) {
            this.otherParty = otherParty;
            this.session = session;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {

            List<SignedTransaction> list = new LinkedList<>();

            session.send("Starting to send");
            final Boolean res = subFlow(new SendMultipleTransactionsFlow(session, new LinkedList<SignedTransaction>()));
            if(res) {
                session.send("Sent with success");
            } else {
                session.send("Failed to send");
            }

            FlowSession otherSession = initiateFlow(otherParty);
            session.send("Starting to send");
            subFlow(new SendMultipleTransactionsFlow(session, new LinkedList<SignedTransaction>()));
            session.send("Sent with success");
            return null;
        }
    }

    public static class SendMultipleTransactionsFlow extends FlowLogic<Boolean> {

        private final FlowSession session;
        private final List<SignedTransaction> transactionList;

        public SendMultipleTransactionsFlow(FlowSession session, List<SignedTransaction> transactionList) {
            this.session = session;
            this.transactionList = transactionList;
        }

        @Suspendable
        @Override
        public Boolean call() throws FlowException {

            for (SignedTransaction transaction : transactionList) {
                session.send(true);
                subFlow(new SendTransactionFlow(session, transaction));
            }
            session.send(false);

            return true;
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
            otherSession.receive(String.class);

            subFlow(new ReceiveMultipleTransactionsFlow(otherSession));
            otherSession.receive(String.class);
            return null;
        }
    }

    public static class ReceiveMultipleTransactionsFlow extends FlowLogic<Void> {

        private final FlowSession session;

        public ReceiveMultipleTransactionsFlow(FlowSession session) {
            this.session = session;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            boolean condition = false;
            while (session.receive(Boolean.class).unwrap(data -> data) || condition) {
                subFlow(new ReceiveTransactionFlow(session, true, StatesToRecord.ALL_VISIBLE));
            }
            return null;
        }
    }

}
