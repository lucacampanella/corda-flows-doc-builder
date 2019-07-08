package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;


import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseText;
import com.github.lucacampanella.callgraphflows.graphics.components2.GSubFlowIndented;
import com.github.lucacampanella.callgraphflows.utils.Utils;


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
        final StatementWithCompanionInterface realCompanion = companion.getRealCompanionStatement();
        if(realCompanion instanceof CordaSubFlow) {
            CordaSubFlow otherFlow = (CordaSubFlow) realCompanion;
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
        graphElem.setEnteringArrowText(initiatingInstruction);

        StringBuilder returnArrowTextBuilder = new StringBuilder();
        if(returnType.isPresent() && !returnType.get().equals("java.lang.Void")) {
            returnArrowTextBuilder.append(Utils.removePackageDescriptionIfWanted(returnType.get()));
        }
        if(returnArrowTextBuilder.length() > 0) {
            final GBaseText exitingTextComponent = new GBaseText(returnArrowTextBuilder.toString());
            exitingTextComponent.setTextColor(GBaseText.LESS_IMPORTANT_TEXT_COLOR);

            graphElem.setExitingArrowText(exitingTextComponent);
        }
    }

    @Override
    public void createGraphLink(StatementWithCompanionInterface companion) {
        final StatementWithCompanionInterface realCompanion = companion.getRealCompanionStatement();
        if(realCompanion instanceof CordaSubFlow) {
            if (isInitiatingFlow() != null && isInitiatingFlow()) {
                this.getInitiatingInstruction().setBrother(((CordaSubFlow) realCompanion).getInitiatingInstruction());
            } else {
                ((CordaSubFlow) realCompanion).getInitiatingInstruction().setBrother(this.getInitiatingInstruction());
            }
        }
    }

    @Override
    public String toString() {
        return "CordaSubFlow<<" + subFlowType.toString() + ">>";
    }
}





