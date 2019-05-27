package com.github.lucacampanella.callgraphflows.Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {

    @Test
    public void removePackageDescription() {
        String res;
        res = Utils.removePackageDescription("ch.adnovum.sb4b.corda.base.flow.commit.CommitTransactionFlow.CommitTrxAcceptor." +
                "SignTxFlow<T> signTxFlow = " +
                "new ch.adnovum.sb4b.corda.base.flow.commit.CommitTransactionFlow.CommitTrxAcceptor.SignTxFlow<T>(input, getCheckers(), " +
                "otherPartyFlow, SignTransactionFlow.Companion.tracker())");
        assertEquals("CommitTransactionFlow.CommitTrxAcceptor.SignTxFlow<T> signTxFlow = " +
                "new CommitTransactionFlow.CommitTrxAcceptor.SignTxFlow<T>(input, getCheckers(), " +
                "otherPartyFlow, SignTransactionFlow.Companion.tracker())", res);

        res = Utils.removePackageDescription("signedTx1 = this.subFlow(new net.corda.core.flows.ReceiveTransactionFlow" +
                "(session, true, net.corda.core.node.StatesToRecord.ALL_VISIBLE))");
        assertEquals("signedTx1 = subFlow(new " +
                "ReceiveTransactionFlow(session, true, StatesToRecord.ALL_VISIBLE))", res);

        res = Utils.removePackageDescription("for(net.corda.core.transactions.SignedTransaction transaction : list)");
        assertEquals( "for(SignedTransaction transaction : list)", res);

        res = Utils.removePackageDescription("signedTx = this.subFlow(new " +
                "net.cardossier.core.flow.car.CreateVinMappingFlow.CreateVinMappingInitiator(new " +
                "net.cardossier.core.flow.car.CreateVinMappingFlow.CreateVinMappingInput(vin)))");

        assertEquals( "signedTx = subFlow(new CreateVinMappingFlow.CreateVinMappingInitiator(new " +
                "CreateVinMappingFlow.CreateVinMappingInput(vin)))", res);

        res = Utils.removePackageDescription("net.corda.core.transactions.SignedTransaction fullySignedTx " +
                "= this.subFlow(new net.corda.core.flows.CollectSignaturesFlow(partSignedTx, " +
                "com.google.common.collect.ImmutableSet.of(otherPartySession), " +
                "net.corda.core.flows.CollectSignaturesFlow.Companion.tracker()))");

        assertEquals("SignedTransaction fullySignedTx = " +
                "subFlow(new CollectSignaturesFlow(partSignedTx, " +
                "ImmutableSet.of(otherPartySession), " +
                "CollectSignaturesFlow.Companion.tracker()))", res);

    }
}