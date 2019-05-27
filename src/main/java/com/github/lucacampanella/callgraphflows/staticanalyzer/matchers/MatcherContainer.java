package com.github.lucacampanella.callgraphflows.staticanalyzer.matchers;

import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.UntrustworthyData;
import spoon.template.TemplateParameter;

/**
 * Class that contains the matchers or patterns that are read by {@link MatcherHelper}
 * and used to query the flow files
 */
public class MatcherContainer extends FlowLogic {

    public TemplateParameter<FlowSession> _flowSession_;

    public TemplateParameter<Object> _any_;

    public TemplateParameter<Party> _party_;

    public TemplateParameter<FlowLogic<SignedTransaction>> _subFlow_;

    public TemplateParameter<Class> _class_;

    public TemplateParameter<Boolean> _bool_;

    public TemplateParameter<SignedTransaction> _signedTrans_;

    public TemplateParameter<FlowSession> _session_;

    public TemplateParameter<UntrustworthyData> _untrustworthyData_;

    public TemplateParameter<UntrustworthyData.Validator> _validator_;

    private void sendMatcher() {
        _flowSession_.S().send(_any_.S());
    }

    private void sendWithBoolMatcher() {
        _flowSession_.S().send(_any_.S(), _bool_.S());
    }

    private void receiveMatcher() {
        _flowSession_.S().receive(_class_.S());
    }

    private void receiveWithBoolMatcher() {
        _flowSession_.S().receive(_class_.S(), _bool_.S());
    }

    private void sendAndReceiveMatcher() {
        _flowSession_.S().sendAndReceive(_class_.S(), _any_.S());
    }

    private void sendAndReceiveWithBoolMatcher() {
        _flowSession_.S().sendAndReceive(_class_.S(), _any_.S(), _bool_.S());
    }

    private void initiateFlowMatcher() {
        initiateFlow(_party_.S());
    }

    private void transactionBuilderMatcher() {
        new TransactionBuilder(_party_.S());
    }

    private void subFlowMatcher() throws FlowException {
        subFlow(_subFlow_.S());
    }

    private void unwrapTemplate() throws FlowException {
        _untrustworthyData_.S().unwrap(_validator_.S());
    }


    private void typeTemplateForFlowLogic() {
        FlowLogic flowLogic;
    }

    private void typeTemplateForFlowSession() {
        FlowSession flowSession;
    }

    private void typeTemplateForSendTransactionFlow() {
        SendTransactionFlow var;
    }

    private void typeTemplateForReceiveTransactionFlow() {
        ReceiveTransactionFlow var;
    }

    private void typeTemplateForSendStateAndRefFlow() {
        SendStateAndRefFlow var;
    }

    private void typeTemplateForReceiveStateAndRefFlow() {
        ReceiveStateAndRefFlow var;
    }

    private void typeTemplateForCollectSignaturesFlow() {
        CollectSignaturesFlow var;
    }

    private void typeTemplateForSignTransactionFlow() {
        SignTransactionFlow var;
    }

    private void typeTemplateForFinalityFlow() {
        FinalityFlow var;
    }

    private void typeTemplateForReceiveFinalityFlow() {
        ReceiveFinalityFlow var;
    }

    @Override
    public Object call() throws FlowException {
        return null;
    }
}
