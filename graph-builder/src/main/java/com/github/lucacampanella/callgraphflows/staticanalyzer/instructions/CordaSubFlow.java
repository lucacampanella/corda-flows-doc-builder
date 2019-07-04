package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.graphics.components2.GSubFlowIndented;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;


public class CordaSubFlow extends SubFlowBase implements StatementWithCompanionInterface {

    GSubFlowIndented graphElem = new GSubFlowIndented();

    protected CordaSubFlow() {

    }

    @Override
    public boolean isCordaSubFlow() {
        return true;
    }

    @Override
    public GSubFlowIndented getGraphElem() {
        return toBePainted() ? graphElem : null;
    }

    @Override
    public boolean acceptCompanion(StatementWithCompanionInterface companion) {
        if(companion instanceof CordaSubFlow) {
            CordaSubFlow otherFlow = (CordaSubFlow) companion;
            if (isInitiatingFlow().equals(otherFlow.isInitiatingFlow())) {
                return false; //they are both either initiating or initiated
            }
            CordaSubFlow initiatingFlow = this.isInitiatingFlow() ? this : otherFlow;
            CordaSubFlow initiatedFlow = otherFlow.isInitiatingFlow() ? this : otherFlow;

            if(SubFlowBuilder.areMatchingSpecialCordaFlow(
                    initiatingFlow.getSubFlowType(), initiatedFlow.getSubFlowType())) {
                return true;
            }
        }
        return false;
    }

    protected void buildGraphElem() {
        graphElem = getMainSubFlowElement();
    }

    @Override
    public void createGraphLink(StatementWithCompanionInterface companion) {
        if(companion instanceof CordaSubFlow) {
            if (isInitiatingFlow() != null && isInitiatingFlow()) {
                this.getInitiatingInstruction().setBrother(((CordaSubFlow) companion).getInitiatingInstruction());
            } else {
                ((CordaSubFlow) companion).getInitiatingInstruction().setBrother(this.getInitiatingInstruction());
            }
        }
    }

    public Branch getInstructionsForCombinations() {
        //we return just the flow if is a corda flow
        return new Branch(this);
    }
}





