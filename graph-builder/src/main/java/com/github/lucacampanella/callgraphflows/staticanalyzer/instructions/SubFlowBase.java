package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseText;
import com.github.lucacampanella.callgraphflows.graphics.components2.GSubFlowIndented;
import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.components2.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import net.corda.core.flows.FlowLogic;
import spoon.reflect.reference.CtTypeReference;

import java.util.Optional;

public abstract class SubFlowBase implements StatementInterface {

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

    @Override
    public String getStringDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("subFlow(");
        sb.append(Utils.removePackageDescriptionIfWanted(subFlowType.toString()));
        sb.append(")");

        return sb.toString();
    }

    protected abstract void buildGraphElem();

    @Override
    public abstract String toString();
}
