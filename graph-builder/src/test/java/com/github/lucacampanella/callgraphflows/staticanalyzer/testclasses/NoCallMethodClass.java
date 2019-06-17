package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public abstract class NoCallMethodClass extends FlowLogic<Void> {

}
