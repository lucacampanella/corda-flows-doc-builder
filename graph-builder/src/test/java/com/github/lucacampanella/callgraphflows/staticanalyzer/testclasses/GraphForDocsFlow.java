package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class GraphForDocsFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<Void> {

        private final Party otherParty;
        private final Party partyToSendTo;

        public Initiator(Party otherParty, Party partyToSendTo) {
            this.otherParty = otherParty;
            this.partyToSendTo = partyToSendTo;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            FlowSession otherSession = initiateFlow(otherParty);
            otherSession.send(partyToSendTo);

            otherSession.receive(String.class);
            return null;
        }
    }

    @InitiatingFlow
    public static class SendMultipleTransactionsFlow extends FlowLogic<Boolean> {

        private final Party party;
        private final List<SignedTransaction> transactionList;

        public SendMultipleTransactionsFlow(Party party, List<SignedTransaction> transactionList) {
            this.party = party;
            this.transactionList = transactionList;
        }

        @Suspendable
        @Override
        public Boolean call() throws FlowException {

            FlowSession session = initiateFlow(party);

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

        private final Party otherParty;

        public Acceptor(Party otherParty) {
            this.otherParty = otherParty;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {

            List<SignedTransaction> list = new LinkedList<>();
            FlowSession session = initiateFlow(otherParty);

            Party partyToSendTo = session.receive(Party.class).unwrap(party -> party);

            final Boolean returnValue =
                    subFlow(new SendMultipleTransactionsFlow(partyToSendTo, new LinkedList<SignedTransaction>()));

            if(returnValue) {
                session.send("Sent with success to the other party");
            } else {
                session.send("Sent unsuccessful");
            }
            return null;
        }
    }

    @InitiatedBy(SendMultipleTransactionsFlow.class)
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
