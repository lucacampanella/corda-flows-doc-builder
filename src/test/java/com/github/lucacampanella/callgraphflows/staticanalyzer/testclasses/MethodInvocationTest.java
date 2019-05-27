package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

public class MethodInvocationTest {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<Void> {

        private static class ClassWithGetter {
            int value;

            public int getValue() {
                return value;
            }
        }

        private final Party otherParty;

        public Initiator(Party otherParty) {
            this.otherParty = otherParty;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {


            final int value = methodWithSessionAsArgumentReturningClassWithGetter(methodReturningASession()).getValue();

            List<SignedTransaction> list = new LinkedList<>();

            FlowSession session = methodReturningASession();

            methodWithSessionAsArgument(session);

            methodWithSessionAsArgument(methodReturningASession());

            session.sendAndReceive(String.class, false);
            return null;
        }

        public ClassWithGetter methodWithSessionAsArgumentReturningClassWithGetter(FlowSession sessionArgument) {
            return new ClassWithGetter();
        }

        public FlowSession methodReturningASession() {
            FlowSession session = initiateFlow(otherParty);
            System.out.println();
            return session;
        }

        public void methodWithSessionAsArgument(FlowSession sessionArgument) {
            FlowSession anotherSession = initiateFlow(otherParty);
            sessionArgument = anotherSession;
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

            otherSession.receive(Boolean.class);
            otherSession.send("Test");
            return null;
        }
    }
}
