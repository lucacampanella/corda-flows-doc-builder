package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class CombinationsHolder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CombinationsHolder.class);
    
    private List<Branch> allCombinations = new LinkedList<>();

    public CombinationsHolder(boolean addEmptyCombination) {
        if(addEmptyCombination) {
            allCombinations.add(new Branch());
        }
    }

    public static CombinationsHolder fromOtherCombination(CombinationsHolder toBeCopied) {
        CombinationsHolder res = new CombinationsHolder(false);
        for(Branch comb : toBeCopied.allCombinations) {
            res.allCombinations.add(new Branch(comb));
        }
        return res;
    }

    public static CombinationsHolder fromSingleStatement(StatementInterface singleStatement) {
        CombinationsHolder res = new CombinationsHolder(false);
        res.addCombination(new Branch(singleStatement));
        return res;
    }

    public void addCombination(Branch comb) {
        allCombinations.add(comb);
    }

    public void appendToAllCombinations(StatementInterface statement) {
        allCombinations.forEach(branch -> branch.add(statement));
    }

    public void appendToAllCombinations(Branch statements) {
        allCombinations.forEach(branch -> branch.add(statements));
    }

    public void combineWithBranch(Branch branch) {
        combineWith(fromBranch(branch));
    }

    public void mergeWith(CombinationsHolder otherHolder) {
        for(Branch comb : otherHolder.allCombinations) {
            allCombinations.add(new Branch(comb));
        }
    }

    public void combineWith(CombinationsHolder otherHolder) {
        if(allCombinations.isEmpty()) {
            for(Branch comb : otherHolder.allCombinations) {
                allCombinations.add(new Branch(comb));
            }
        }
        if(otherHolder.allCombinations.size() == 1) { //more efficient way if only one branch on other side
            otherHolder.allCombinations.get(0).forEach(this::appendToAllCombinations);
        }
        else if(!otherHolder.allCombinations.isEmpty()){
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

        for(StatementInterface instr : instructions) {
            holder.combineWith(instr.getResultingCombinations());
        }

        return holder;
    }

    public boolean isEmpty() {
        return allCombinations.isEmpty();
    }

    public boolean checkIfMatchesAndDraw(CombinationsHolder otherCombinationsHolder) {
        boolean foundOneMatch = false;
        for(Branch combLeft : this.allCombinations) {
            for(Branch combRight : otherCombinationsHolder.allCombinations) {
                final List<MatchingStatements> matchingStatements = twoCombinationsMatch(combLeft, combRight);
                if(matchingStatements != null) {
                    foundOneMatch = true;
                    matchingStatements.forEach(MatchingStatements::createGraphLink);
                }
            }
        }
        return foundOneMatch;
    }

    private static class MatchingStatements {
        StatementWithCompanionInterface leftStatement;
        StatementWithCompanionInterface rightStatement;

        public MatchingStatements(StatementWithCompanionInterface leftStatement, StatementWithCompanionInterface rightStatement) {
            this.leftStatement = leftStatement;
            this.rightStatement = rightStatement;
        }

        public void createGraphLink() {
            leftStatement.createGraphLink(rightStatement);
        }
    }

    /**
     * Checks if two branches have a matching combination and returns a list of all the
     * matching statements if they match, an empty list if there is nothing to match (still a valid
     * protocol) or null if the two combinations don't match
     * @param combLeft initiating branch
     * @param combRight initiated branch
     * @return a list of all the
     *       matching statements if they match, an empty list if there is nothing to match (still a valid
     *       protocol) or null if the two combinations don't match
     */
    private static List<MatchingStatements> twoCombinationsMatch(Branch combLeft,
                                                Branch combRight) {
        Deque<StatementWithCompanionInterface> initiatingQueue =
                new LinkedList<>(combLeft.getOnlyStatementWithCompanionStatements()); //they should all be already fo this type
        Deque<StatementWithCompanionInterface> initiatedQueue =
                new LinkedList<>(combRight.getOnlyStatementWithCompanionStatements());

        List<MatchingStatements> matchingStatements = new ArrayList<>();
        int i = 0;

        while(!initiatingQueue.isEmpty() || !initiatedQueue.isEmpty()) {

            StatementWithCompanionInterface instrLeft = initiatingQueue.peek();
            StatementWithCompanionInterface instrRight = initiatedQueue.peek();
            if (instrLeft == null && instrRight == null) {
                return matchingStatements;
            }
            if(instrLeft == null || instrRight == null) {
                return null; //one fot the two queues still has elements, while the other doesn't
            }

            LOGGER.trace("\n Round {}", i++);
            LOGGER.trace("{}", instrLeft);
            LOGGER.trace("{}", instrRight);

            if(!instrLeft.acceptCompanion(instrRight)) {
                LOGGER.info("error in this flow logic!");
                return null;
            }
            else {
                matchingStatements.add(new MatchingStatements(instrLeft, instrRight));
            }

            if(instrLeft.isConsumedForCompanionAnalysis()) {
                initiatingQueue.remove(); //we remove the statement of the queue
            }

            if(instrRight.isConsumedForCompanionAnalysis()) {
                initiatedQueue.remove(); //we remove the statement of the queue
            }
        }

        return matchingStatements;
    }
}
