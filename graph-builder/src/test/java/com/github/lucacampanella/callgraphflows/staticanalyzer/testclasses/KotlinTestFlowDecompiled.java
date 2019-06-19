package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;/*
 * Decompiled with CFR 0.135.
 * 
 * Could not load the following classes:
 *  KotlinTestFlow$Responder$call
 *  KotlinTestFlow$Responder$call$signTransactionFlow
 */
import co.paralleluniverse.fibers.Suspendable;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public final class KotlinTestFlowDecompiled {
    public static final KotlinTestFlowDecompiled INSTANCE;

    private KotlinTestFlowDecompiled() {
    }

    static {
        KotlinTestFlowDecompiled kotlinTestFlow;
        INSTANCE = kotlinTestFlow = new KotlinTestFlowDecompiled();
    }

    @InitiatingFlow
    @StartableByRPC
    public static final class Initiator
    extends FlowLogic<Unit> {
        @NotNull
        private final ProgressTracker progressTracker;
        @NotNull
        private final Party counterparty;

        @NotNull
        @Override
        public ProgressTracker getProgressTracker() {
            return this.progressTracker;
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            TransactionBuilder txBuilder = new TransactionBuilder(this.counterparty);
            SignedTransaction partStx = this.getServiceHub().signInitialTransaction(txBuilder);
            FlowSession counterpartySession = this.initiateFlow(this.counterparty);
            SignedTransaction fullyStx = (SignedTransaction) this.subFlow(new CollectSignaturesFlow(partStx, Collections.singleton(counterpartySession)));

            return null;
        }

        @NotNull
        public final Party getCounterparty() {
            return this.counterparty;
        }

        public Initiator(@NotNull Party counterparty) {
            Intrinsics.checkParameterIsNotNull(counterparty, "counterparty");
            this.counterparty = counterparty;
            this.progressTracker = new ProgressTracker(new ProgressTracker.Step[0]);
        }
    }

    @InitiatedBy(value=Initiator.class)
    public static final class Responder
    extends FlowLogic<Unit> {
        @NotNull
        private final FlowSession counterpartySession;

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            SignTransactionFlow signTransactionFlow2 = null;
            SecureHash txId = ((SignedTransaction)this.subFlow(signTransactionFlow2)).getId();
            this.subFlow(new ReceiveFinalityFlow(this.counterpartySession, txId));

            return null;
        }

        @NotNull
        public final FlowSession getCounterpartySession() {
            return this.counterpartySession;
        }

        public Responder(@NotNull FlowSession counterpartySession) {
            Intrinsics.checkParameterIsNotNull(counterpartySession, "counterpartySession");
            this.counterpartySession = counterpartySession;
        }
    }

}

