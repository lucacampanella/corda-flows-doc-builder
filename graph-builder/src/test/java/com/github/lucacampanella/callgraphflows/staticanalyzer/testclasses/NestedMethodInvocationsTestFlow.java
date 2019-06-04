package com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.LinkedList;
import java.util.List;

    @InitiatingFlow
    @StartableByRPC
    public class NestedMethodInvocationsTestFlow extends FlowLogic<Void> {

        private static class ClassWithSendInConstructor {
            FlowSession internalSession;

            public ClassWithSendInConstructor(FlowSession internalSession) {
                this.internalSession = internalSession;
                internalSession.send("this is a send string");
            }
        }

        private static class ClassWithGetter {
            ClassWithGetter value;

            public ClassWithGetter getValue() {
                return value;
            }
        }

        private static class BoolClassWithGetter {
            boolean value;

            public boolean getValue() {
                return value;
            }
        }

        private final Party otherParty;

        public NestedMethodInvocationsTestFlow(Party otherParty) {
            this.otherParty = otherParty;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {


            List<SignedTransaction> list = new LinkedList<>();

            FlowSession session = methodReturningASession();

            ClassWithSendInConstructor classWithSendInConstructor =
                    new ClassWithSendInConstructor(methodWithASendReturningASession(session));

            new ClassWithSendInConstructor(session);

            methodWithASend(session);

            BoolClassWithGetter boolClassWithGetter = new BoolClassWithGetter();
            BoolClassWithGetter boolClassWithGetter1 = new BoolClassWithGetter();
            if(boolClassWithGetter.getValue() || boolClassWithGetter1.getValue()) {
                session.send(5);
            }

            session.send(methodWithTwoClassWithGetterAsArgumentReturningClassWithGetter(methodReturningAClassWithGetter(),
                    methodReturningAClassWithGetter().getValue()).getValue());

            methodWithSessionAsArgument(session);

            methodWithSessionAsArgument(methodReturningASession());

            session.sendAndReceive(String.class, false);
            return null;
        }

        public FlowSession methodWithASendReturningASession(FlowSession mySession) {
            mySession.send("this is a string");
            return mySession;
        }

        public void methodWithASend(FlowSession mySession) {
            mySession.send("this is a string");
        }

        public ClassWithGetter methodWithSessionAsArgumentReturningClassWithGetter(FlowSession sessionArgument) {
            return new ClassWithGetter();
        }

        public ClassWithGetter methodWithTwoClassWithGetterAsArgumentReturningClassWithGetter
                (ClassWithGetter arg1, ClassWithGetter ag2) {
            return new ClassWithGetter();
        }

        public ClassWithGetter methodReturningAClassWithGetter() {
            return new ClassWithGetter();
        }

        public FlowSession methodReturningASession() {
            FlowSession session = initiateFlow(otherParty);

            return session;
        }

        public void methodWithSessionAsArgument(FlowSession sessionArgument) {
            FlowSession anotherSession = initiateFlow(otherParty);
            sessionArgument = anotherSession;
        }
    }
