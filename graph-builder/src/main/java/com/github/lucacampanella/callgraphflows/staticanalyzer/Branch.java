package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.InitiateFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Branch implements Iterable<StatementInterface> {
    List<StatementInterface> statements;

    public Branch(List<StatementInterface> statements) {
        this.statements = statements;
    }

    public Branch() {
        statements = new LinkedList<>();
    }

    public Branch(StatementInterface singleInstr) {
        this();
        statements.add(singleInstr);
    }

    public void add(StatementInterface instr) {
        if(instr != null) statements.add(instr);
    }

    public boolean addIfRelevantForAnalysis(StatementInterface instr) {
        if (instr != null && instr.isRelevantForAnalysis()) {
            add(instr);
            return true;
        }
        return false;
    }

    public boolean addIfRelevantForAnalysis(Branch instrs) {
        boolean atLeastOneAdded = false;
        for(StatementInterface stmt : instrs) {
            boolean addingRes = addIfRelevantForAnalysis(stmt);
            atLeastOneAdded = atLeastOneAdded || addingRes;
        }
        return atLeastOneAdded;
    }

    public boolean addIfRelevantForAnalysisOrIfRelevantForLoop(StatementInterface instr) {
        if (instr != null && (instr.isRelevantForAnalysis() || instr.isRelevantForLoop())) {
            add(instr);
            return true;
        }
        return false;
    }

    public boolean addIfRelevantForAnalysisOrIfRelevantForLoop(Branch instrs) {
        boolean atLeastOneAdded = false;
        for(StatementInterface stmt : instrs) {
            boolean addingRes = addIfRelevantForAnalysisOrIfRelevantForLoop(stmt);
            atLeastOneAdded = atLeastOneAdded || addingRes;
        }
        return atLeastOneAdded;
    }

    public List<StatementInterface> getStatements() {
        return statements;
    }

    public void add(Branch branch) {
        this.statements.addAll(branch.getStatements());
    }

    public void addAll(List<StatementInterface> statementsToAppend) {
        statements.addAll(statementsToAppend);
    }

    public boolean isEmpty() {
        return getStatements().isEmpty();
    }

    public boolean isRelevant() {
        return statements.stream().anyMatch(StatementInterface::isRelevantForAnalysis);
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
        return statements.stream().map(stmt -> stmt.getInitiateFlowStatementAtThisLevel()).filter(Optional::isPresent)
                .findFirst().orElse(Optional.empty());
    }

    @Override
    public String toString() {
        return statements.toString();
    }
}
