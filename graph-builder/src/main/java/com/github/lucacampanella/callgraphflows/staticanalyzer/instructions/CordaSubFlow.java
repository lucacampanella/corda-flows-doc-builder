package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.puml.PBaseComponent;
import com.github.lucacampanella.callgraphflows.graphics.puml.PSubFlow;
import com.github.lucacampanella.callgraphflows.graphics.svg.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;


public class CordaSubFlow extends SubFlowBase implements StatementWithCompanionInterface {

    GSubFlow graphElem = new GSubFlow();

    protected CordaSubFlow() {

    }

    @Override
    public boolean isCordaSubFlow() {
        return true;
    }

    @Override
    public GSubFlow getGraphElem() {
        return toBePainted() ? graphElem : null;
    }

    @Override
    protected void buildGraphElem() {
        graphElem = getMainSubFlowElement();
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

    @Override
    public void createGraphLink(StatementWithCompanionInterface companion) {
        if(companion instanceof CordaSubFlow) {
            if (isInitiatingFlow() != null && isInitiatingFlow()) {
                this.getInitiatingInstruction().addBrother(((CordaSubFlow) companion).getInitiatingInstruction());
            } else {
                ((CordaSubFlow) companion).getInitiatingInstruction().addBrother(this.getInitiatingInstruction());
            }
        }
    }

    public Branch getInstructionsForCombinations() {
        //we return just the flow if is a corda flow
        return new Branch(this);
    }
}





