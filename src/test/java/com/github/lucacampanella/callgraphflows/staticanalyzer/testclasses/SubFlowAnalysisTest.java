package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

import java.util.LinkedList;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class SubFlowAnalysisTest {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final Party otherParty;

        public Initiator(Party otherParty) {
            this.otherParty = otherParty;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            FlowSession otherPartySession = initiateFlow(otherParty);

            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(null);

            final CollectSignaturesFlow collectSignaturesFlow = new CollectSignaturesFlow(partSignedTx,
                    ImmutableSet.of(otherPartySession), CollectSignaturesFlow.Companion.tracker());

            final SignedTransaction fullySignedTx = subFlow(collectSignaturesFlow);

            return subFlow(new FinalityFlow(fullySignedTx, ImmutableSet.of(otherPartySession)));
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {

        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                super(otherPartyFlow, progressTracker);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) {

            }
        }

        private final FlowSession otherPartySession;

        public Acceptor(FlowSession otherSession) {
            this.otherPartySession = otherSession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            boolean condition = true;

            final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
            final SecureHash txId = subFlow(signTxFlow).getId();

            SignedTransaction signedTx = subFlow(new ReceiveFinalityFlow(otherPartySession, txId));

            ///return subFlow(new ReceiveFinalityFlow(otherPartySession, txId));
            return signedTx;
        }
    }
}
