package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class SubFlowInitializationTest {

        @InitiatingFlow
        @StartableByRPC
        public static class Initiator extends FlowLogic<SignedTransaction> {

            private final Party otherParty;


            public Initiator(Party otherParty) {
                this.otherParty = otherParty;
            }

            /**
             * The flow logic is encapsulated within the call() method.
             */
            @Suspendable
            @Override
            public SignedTransaction call() throws FlowException {
                // Obtain a reference to the notary we want to use.
                final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

                // Stage 1.
                // Generate an unsigned transaction.
                Party me = getOurIdentity();

                final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                        .addCommand(new Command<CommandData>(new CommandData() {
                        }, me.getOwningKey()));

                // Stage 2.
                txBuilder.verify(getServiceHub());


                // Sign the transaction.
                final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

                // Send the state to the counterparty, and receive it back with their signature.
                FlowSession otherPartySession = initiateFlow(otherParty);

                boolean condition = true;
                boolean condition2 = true;
                otherPartySession.send(true);
                if(condition) {
                    otherPartySession.send(new Object());
                }
//            else if(condition2) {
//                otherPartySession.receive(String.class);
//            }
                else {
                    otherPartySession.receive(Object.class);
                }

//            if(condition) {
//                otherPartySession.send(new Object());
//            }
//            else {
//                otherPartySession.send(new String());
//            }


                final SignedTransaction fullySignedTx = subFlow(
                        new CollectSignaturesFlow(partSignedTx, ImmutableSet.of(otherPartySession), CollectSignaturesFlow.Companion.tracker()));

                // Stage 5.
                // Notarise and record the transaction in both parties' vaults.
                return subFlow(new FinalityFlow(fullySignedTx, ImmutableSet.of(otherPartySession)));
            }
        }

        @InitiatedBy(Initiator.class)
        public static class Acceptor extends FlowLogic<SignedTransaction> {

            private final FlowSession otherPartySession;

            private FlowSession anotherRandomSession = null;

            public Acceptor(FlowSession otherPartySession) {
                this.otherPartySession = otherPartySession;
            }

            @Suspendable
            @Override
            public SignedTransaction call() throws FlowException {
                class SignTxFlow extends SignTransactionFlow {
                    private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                        super(otherPartyFlow, progressTracker);
                    }

                    @Override
                    protected void checkTransaction(SignedTransaction stx) {

                    }
                }
                boolean condition = true;

//                FlowSession testSession = null;
//
//                int x = 0;
//                if(condition) {
//                    x = 5;
//                    testSession = otherPartySession;
//                } else {
//                    testSession = null;
//                }
//
//
//                if(otherPartySession.receive(Boolean.class).unwrap(data -> data)) {
//                    otherPartySession.receive(Object.class);
//                }
//                else {
//                    otherPartySession.send(new Object());
//                }

//           if(condition) {
//               otherPartySession.send(new Object());
//            }
//            else {
//               otherPartySession.send(new Object());
//            }

                anotherRandomSession = initiateFlow(getOurIdentity()); //just to test, doesn't make sense to
                //initialize it on ourselves

                final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
                final SignTxFlow signTxFlow2 = new SignTxFlow(anotherRandomSession, SignTransactionFlow.Companion.tracker());

                boolean condition2= false;

                SignTxFlow otherSignTxFlow = null;
                if(condition) {
                    otherSignTxFlow = signTxFlow;
                } else if(condition2){
                    otherSignTxFlow = signTxFlow2;
                }
                else {
                    otherSignTxFlow = null;
                }
                final SecureHash txId = subFlow(otherSignTxFlow).getId();

                SignedTransaction signedTx = subFlow(new ReceiveFinalityFlow(otherPartySession, txId));

                ///return subFlow(new ReceiveFinalityFlow(otherPartySession, txId));
                return signedTx;
            }
        }
    }

