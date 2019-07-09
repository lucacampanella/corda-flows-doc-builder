package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.InitiateFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementWithCompanionInterface;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class Branch implements Iterable<StatementInterface> {
    List<StatementInterface> statements;

    public Branch(List<StatementInterface> statements) {
        this.statements = statements;
    }

    public Branch(Branch toCopy) {
        this.statements = new ArrayList<>(toCopy.getStatements());
    }

    public Branch() {
        statements = new LinkedList<>();
    }

    public Branch(StatementInterface singleInstr) {
        this();
        statements.add(singleInstr);
    }

    public void add(StatementInterface instr) {
        if(instr != null) {
            statements.add(instr);
        }
    }

    public void addIfRelevantForLoopFlowBreakAnalysis(StatementInterface instr) {
        if (instr != null && instr.isRelevantForLoopFlowBreakAnalysis()) {
            add(instr);
        }
    }

    public void addIfRelevantForLoopFlowBreakAnalysis(Branch instrs) {
        for(StatementInterface stmt : instrs) {
            addIfRelevantForLoopFlowBreakAnalysis(stmt);
        }
    }

    public List<StatementInterface> getStatements() {
        return statements;
    }

    public List<StatementWithCompanionInterface> getOnlyStatementWithCompanionStatements() {
        return statements.stream().filter(StatementInterface::needsCompanion)
                .map(stmt -> (StatementWithCompanionInterface) stmt).collect(Collectors.toList());
    }

    public void add(Branch branch) {
        for(StatementInterface stmt : branch) {
            add(stmt);
        }
    }

    public boolean isEmpty() {
        return getStatements().isEmpty();
    }

    public boolean isRelevantForLoopFlowBreakAnalysis() {
        return statements.stream().anyMatch(StatementInterface::isRelevantForLoopFlowBreakAnalysis);
    }

    public boolean isRelevantForMethodFlowBreakAnalysis() {
        return statements.stream().anyMatch(StatementInterface::isRelevantForMethodFlowBreakAnalysis);
    }

    public boolean isRelevantForProtocolAnalysis() {
        return statements.stream().anyMatch(StatementInterface::isRelevantForProtocolAnalysis);
    }

    public boolean toBePainted() {
        return statements.stream().anyMatch(StatementInterface::toBePainted);
    }

    @NotNull
    @Override
    public Iterator<StatementInterface> iterator() {
        return statements.iterator();
    }

    public Optional<InitiateFlow> getInitiateFlowStatementAtThisLevel() {
        return statements.stream().map(StatementInterface::getInitiateFlowStatementAtThisLevel)
                .filter(Optional::isPresent).findFirst().orElse(Optional.empty());
    }

    public boolean hasSendOrReceiveAtThisLevel() {
        return statements.stream().anyMatch(StatementInterface::hasSendOrReceiveAtThisLevel);
    }

    @Override
    public String toString() {
        return statements.toString();
    }


    public boolean allInitiatingFlowsHaveValidProtocolAndSetupLinks() {
        return statements.stream().allMatch(StatementInterface::checkIfContainsValidProtocolAndSetupLinks);
    }

    public boolean containsSameStatementsAs(Branch otherBranch) {
        final List<StatementInterface> otherStatements = otherBranch.getStatements();
        if(this.statements.size() != otherStatements.size()) {
            return false;
        }
        for(int i = 0; i < statements.size(); ++i) {
            if(statements.get(i) != otherStatements.get(i)) {
                return false;
            }
        }
        return true;
    }
}
