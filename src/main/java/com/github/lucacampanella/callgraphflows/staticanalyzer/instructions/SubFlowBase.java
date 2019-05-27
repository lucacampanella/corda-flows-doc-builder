package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.Utils.Utils;
import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GInstruction;
import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import net.corda.core.flows.FlowLogic;
import spoon.reflect.reference.CtTypeReference;

import java.util.Optional;

abstract class SubFlowBase implements StatementInterface {

    //String subFlowType;
    CtTypeReference<? extends FlowLogic> subFlowType = null;
    Optional<String> assignedVariableName = Optional.empty(); //if the subflow returns an object and assigns it
    //to a variable this is present and contains the variable name

    int line = 0;

    Branch internalMethodInvocations = new Branch();

    Optional<String> returnType;

    Optional<String> subFlowVariableName = Optional.empty(); //if the subFlow is called with a variable as a
    //parameter and not with a constructor this is present

    protected Optional<String> targetSessionName = Optional.empty();

    Boolean isInitiatingFlow = null;

    GInstruction initiatingInstruction; //this is the call to subFlow

//AnalysisResult resultOfClassAnalysis = new AnalysisResult();

    public boolean isCordaSubFlow() {
        return false;
    }
    public boolean isInitiatingSubFlow() {
        return false;
    }
    public boolean isInlinableSubFlow() {
        return false;
    }

    @Override
    public Optional<String> getTargetSessionName() {
        return targetSessionName;
    }

    @Override
    public Branch getInternalMethodInvocations() {
        return internalMethodInvocations;
    }

    public Boolean isInitiatingFlow() {
        return isInitiatingFlow;
    }

    public CtTypeReference<? extends FlowLogic> getSubFlowType() {
        return subFlowType;
    }

    public Optional<String> getAssignedVariableName() {
        return assignedVariableName;
    }

    public int getLine() {
        return line;
    }

    public Optional<String> getReturnType() {
        return returnType;
    }

    public Optional<String> getSubFlowVariableName() {
        return subFlowVariableName;
    }

    public Boolean getInitiatingFlow() {
        return isInitiatingFlow;
    }

    public GInstruction getInitiatingInstruction() {
        return initiatingInstruction;
    }

    //
//    public String getSubFlowType() {
//        return subFlowType;
//    }

    @Override
    public String getStringDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("subFlow ");
        sb.append("<<");
        sb.append(Utils.removePackageDescriptionIfWanted(subFlowType.toString()));
        sb.append(">>");

        return sb.toString();
    }

    protected GSubFlow getMainSubFlowElement() {
        initiatingInstruction = new GInstruction(line, getStringDescription());

        final GSubFlow mainSubFlow = new GSubFlow();

        mainSubFlow.setEnteringArrowText(initiatingInstruction);

        StringBuilder returnArrowTextBuilder = new StringBuilder();
        //assignedVariableName.ifPresent(returnArrowTextBuilder::append);
        if(returnType.isPresent() && !returnType.get().equals("java.lang.Void")) {
            returnArrowTextBuilder.append(" <<");
            returnArrowTextBuilder.append(Utils.removePackageDescriptionIfWanted(returnType.get()));
            returnArrowTextBuilder.append(">>");
        }
        if(returnArrowTextBuilder.length() > 0) {
            final GBaseTextComponent exitingTextComponent = new GBaseTextComponent(returnArrowTextBuilder.toString());
            exitingTextComponent.setTextColor(GBaseTextComponent.LESS_IMPORTANT_TEXT_COLOR);

            mainSubFlow.setExitingArrowText(exitingTextComponent);
        }

        return mainSubFlow;
    }

    protected abstract void buildGraphElem();

    //we return just the flow if is a corda flow or if is an initiating flow
    //this will result on acceptCompanion being called on this instance
    //for carda flows we'll just check if the flow matches
    //for initiating flow we'll get the other class and compare if they have at least one matching combination

    //if the flow doesn't initiate anything than we just inline it, for analysis is the same
    //we keep the subFlow call because we need the map of sessions passed
    public abstract Branch getInstructionsForCombinations();

}
