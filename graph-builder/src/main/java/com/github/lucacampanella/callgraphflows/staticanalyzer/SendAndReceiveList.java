package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.InstructionStatement;

import java.util.LinkedList;
import java.util.List;

public class SendAndReceiveList {
    List<InstructionStatement> statements;

    public SendAndReceiveList(List<InstructionStatement> statements) {
        this.statements = statements;
    }

    public SendAndReceiveList() {
        statements = new LinkedList<>();
    }

    public SendAndReceiveList(InstructionStatement singleInstr) {
        this();
        statements.add(singleInstr);
    }

    public void appendStatement(InstructionStatement instr) {
        statements.add(instr);
    }

    public List<InstructionStatement> getStatements() {
        return statements;
    }

    public void append(SendAndReceiveList list) {
        this.statements.addAll(list.getStatements());
    }

    public void appendStatements(List<InstructionStatement> statementsToAppend) {
        statements.addAll(statementsToAppend);
    }

    public boolean isEmpty() {
        return getStatements().isEmpty();
    }
}
