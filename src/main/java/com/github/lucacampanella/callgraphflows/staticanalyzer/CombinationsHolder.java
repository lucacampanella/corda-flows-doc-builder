package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.*;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class CombinationsHolder {
    private List<Branch> allCombinations = new LinkedList<>();

    public CombinationsHolder(boolean addEmptyCombination) {
        if(addEmptyCombination) {
            allCombinations.add(new Branch());
        }
    }

    public void addCombination(Branch comb) {
        allCombinations.add(comb);
    }

    public void appendToAllCombinations(StatementInterface statement) {
        allCombinations.forEach(branch -> branch.add(statement));
    }

    public void combineWithBranch(Branch branch) {
        combineWith(fromBranch(branch));
    }

    public void mergeWith(CombinationsHolder otherHolder) {
        allCombinations.addAll(otherHolder.allCombinations);
    }

    public void combineWith(CombinationsHolder otherHolder) {
        //allCombinations.forEach(branch -> branch.add(statement));
        if(otherHolder.allCombinations.size() == 1) { //more efficient way if only one branch on other side
            otherHolder.allCombinations.get(0).forEach(this::appendToAllCombinations);
        }
        else {
            List<Branch> newAllCombinations = new LinkedList<>();
            for (Branch currBranch : allCombinations) {
                for (Branch newBranch : otherHolder.allCombinations) {
                    Branch bothTogether = new Branch();
                    bothTogether.add(currBranch);
                    bothTogether.add(newBranch);
                    newAllCombinations.add(bothTogether);
                }
            }
            allCombinations = newAllCombinations;
        }
    }

    //starting from a desugared branch
    public static CombinationsHolder fromBranch(Branch instructions) {
        CombinationsHolder holder = new CombinationsHolder(true);
//        List<List<StatementInterface>> result = new LinkedList<>();
//        result.add(new LinkedList<>());

        for(StatementInterface instr : instructions) {
            if(!instr.getInternalMethodInvocations().isEmpty()) {
                //todo: here we should have already desugared, nothing should be present and this line never happen
                final Branch flatInternalInvocations = instr.flattenInternalMethodInvocations();
                holder.combineWithBranch(flatInternalInvocations);
            }
            if(instr instanceof InitiatingSubFlow) {
                holder.combineWithBranch(((InitiatingSubFlow) instr).getInstructionsForCombinations());
            } else {
                holder.appendToAllCombinations(instr);
            }
            if(instr instanceof MethodInvocation) {
                holder.combineWithBranch(((MethodInvocation) instr).getBody());
            }
            else if(instr instanceof BranchingStatement) {
                //also add empty branches because they mean the empty road can be taken
                //but null means that branching statement only has one way (not really a branch there, only used for
                //do-while here)
                CombinationsHolder mergedCombination = new CombinationsHolder(false);
                if(((BranchingStatement) instr).getBranchTrue() != null) {
                    mergedCombination.mergeWith(fromBranch(((BranchingStatement) instr).getBranchTrue()));
                }
                if(((BranchingStatement) instr).getBranchFalse() != null) {
                    mergedCombination.mergeWith(fromBranch(((BranchingStatement) instr).getBranchFalse()));
                }
                holder.combineWith(mergedCombination);
                System.out.println();
            }
        }

        return holder;
    }

    public boolean isEmpty() {
        return allCombinations.isEmpty();
    }

    public boolean hasOneMatchWith(CombinationsHolder otherCombinationsHolder) {
        for(Branch combLeft : this.allCombinations) {
            for(Branch combRight : otherCombinationsHolder.allCombinations) {
                if(twoCombinationsMatch(combLeft, combRight)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean twoCombinationsMatch(Branch combLeft,
                                                Branch combRight) {
        Deque<StatementInterface> initiatingQueue = new LinkedList<>(combLeft.getStatements());
        Deque<StatementInterface> initiatedQueue = new LinkedList<>(combRight.getStatements());

        int i = 0;

        while(!initiatingQueue.isEmpty() || !initiatedQueue.isEmpty()) {
            StatementInterface instrLeft = StaticAnalyzer.consumeUntilBlockingOrBranch(initiatingQueue);
            StatementInterface instrRight = StaticAnalyzer.consumeUntilBlockingOrBranch(initiatedQueue);
            if (instrLeft == null && instrRight == null) {
                return true;
            }
            if((instrLeft == null && instrRight != null) || (instrLeft != null && instrRight == null)) {
                return false;
            }
            if (instrLeft instanceof BranchingStatement) {
                //if it has a blocking statement in the condition than we need to keep it in the stack and
                //see if accepts the companion on the other side

                BranchingStatement branchingStatement = ((BranchingStatement) instrLeft);
                initiatingQueue.remove();
                if(branchingStatement.hasBlockingStatementInCondition()) {
                    initiatingQueue.addFirst(branchingStatement.getBlockingStatementInCondition());
                }
                continue;
            }
            if(instrRight instanceof BranchingStatement) {
                initiatedQueue.remove();
                BranchingStatement branchingStatement = ((BranchingStatement) instrRight);
                if(branchingStatement.hasBlockingStatementInCondition()) {
                    initiatedQueue.addFirst(branchingStatement.getBlockingStatementInCondition());
                }
                continue;
            }

            StatementWithCompanionInterface statementLeft = (StatementWithCompanionInterface) instrLeft;
            StatementWithCompanionInterface statementRight = (StatementWithCompanionInterface) instrRight;

            System.out.println("\n Round " + i++);
            System.out.println(statementLeft);
            System.out.println(statementRight);

            if(!(statementLeft instanceof SendAndReceive) || ((SendAndReceive) statementLeft).isSentConsumed()) {
                initiatingQueue.remove(); //we remove the statement of the queue
            }

            if(!(statementRight instanceof SendAndReceive) || ((SendAndReceive) statementRight).isSentConsumed()) {
                initiatedQueue.remove(); //we remove the statement of the queue
            }

            if(!statementLeft.acceptCompanion(statementRight)) {
                System.out.println("**** ERROR in flow logic!");

                return false;
            }
        }

        return true;
    }

//    public boolean hasOneMatchWith(CombinationsHolder holder) {
//
//    }
}
