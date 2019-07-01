package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.*;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CombinationWithSessions {

    private static class Session {
        private final StatementInterface initiatingStatement; //this can either be an initiateFlow or any other
        //call that has as target a variable we never saw before (so it's a field or an argument)

        public Session(StatementInterface initiatingStatement) {
            this.initiatingStatement = initiatingStatement;
        }
    }

    private static class SubFlowToSession {
        private final StatementInterface initiatingStatement; //this can either be a call to a constructor
        //of a class that is used as a subFlow later on or any other
        //call that has as target a variable we never saw before (so it's a field or an argument)
        private final CombinationWithSessions.Session pointedSession;

        public SubFlowToSession(StatementInterface initiatingStatement, CombinationWithSessions.Session pointedSession) {
            this.initiatingStatement = initiatingStatement;
            this.pointedSession = pointedSession;
        }

        public CombinationWithSessions.Session getPointedSession() {
            return pointedSession;
        }
    }


    List<StatementInterface> statements;
    Map<Session, List<StatementInterface>> sessionToStatementMap = new HashMap<>();


    Map<String, Session> nameToSessionMap = new HashMap<>();
    Map<String, SubFlowToSession> nameToSubFlowMap = new HashMap<>();

    private static final Session nullSession = new Session(null); //it's a session to represent statements
    //that have as target a null session, this can be statically detected and it's clearly an error

    private static final Session noSessionSession = new Session(null); //it's a session to represent statements
    //that are nice to show but don't have a matching session, like transaction builders

    public CombinationWithSessions(List<StatementInterface> statements) {

        this.statements = statements;


        nameToSessionMap.put("null", nullSession);

        statements.forEach(stmt -> {
            Session discoveredSession = null;
            if(stmt.modifiesSession()) {
                if(stmt instanceof InitiateFlow) {
                    discoveredSession = new Session(stmt);
                    nameToSessionMap.put(stmt.getTargetSessionName().orElse("1default"), discoveredSession);
                }
                else if(stmt instanceof SessionAssignment){ //assignment to session variable, we have to map to names to same session
                    SessionAssignment assignment = ((SessionAssignment) stmt);
                    //default case: lhs points now to session on rhs
                    if (nameToSessionMap.containsKey(assignment.getRhsName())) {
                        discoveredSession = nameToSessionMap.get(assignment.getRhsName());
                        nameToSessionMap.put(assignment.getLhsName(), discoveredSession);

                    }
                    else { //no rhs was in the map -> this means it is probably a field or a parameter
                        discoveredSession = new Session(stmt);
                        nameToSessionMap.put(assignment.getLhsName(), discoveredSession);
                        nameToSessionMap.put(assignment.getRhsName(), discoveredSession);

                    }
                }
            }
            else {
                if(!stmt.getTargetSessionName().isPresent()) {
                    discoveredSession = noSessionSession;
                }
                else {
                    String targetSessionName = stmt.getTargetSessionName().get();
                    if (!nameToSessionMap.containsKey(targetSessionName)) {
                        //no name was in the map -> this means it is probably a field or a parameter
                        discoveredSession = new Session(stmt);
                        nameToSessionMap.put(targetSessionName, discoveredSession);
                    }
                    else {
                        discoveredSession = nameToSessionMap.get(targetSessionName);
                    }
                }
            }


            if(stmt.modifiesFlow()) {
                if(stmt instanceof FlowConstructor) {
                    nameToSubFlowMap.put(((FlowConstructor) stmt).getLhsName(),
                            new SubFlowToSession(stmt, discoveredSession));
                }
                else if(stmt instanceof FlowAssignment){ //assignment to session flow, we have to map to names to same flow
                    FlowAssignment assignment = ((FlowAssignment) stmt);
                    //default case: lhs points now to session on rhs
                    if (nameToSubFlowMap.containsKey(assignment.getRhsName())) {
                        final SubFlowToSession subFlowToSession = nameToSubFlowMap.get(assignment.getRhsName());
                        nameToSubFlowMap.put(assignment.getLhsName(), subFlowToSession);

                    }
                    else { //no rhs was in the map -> this means it is probably a field or a parameter
                        final SubFlowToSession subFlowToSession = new SubFlowToSession(stmt, discoveredSession);
                        nameToSubFlowMap.put(assignment.getLhsName(), subFlowToSession);
                        nameToSubFlowMap.put(assignment.getRhsName(), subFlowToSession);
                    }
                }
            }

            if(stmt instanceof InitiatingSubFlow) {
                if(stmt.getTargetSessionName().isPresent()) { //this means it was created here, either by
                    // constructor call or by method call
                    String targetSessionName = stmt.getTargetSessionName().get();
                    if (!nameToSessionMap.containsKey(targetSessionName)) {
                        //no name was in the map -> this means it is probably a field or a parameter
                        discoveredSession = new Session(stmt);
                        nameToSessionMap.put(targetSessionName, discoveredSession);
                    }
                    else {
                        discoveredSession = nameToSessionMap.get(targetSessionName);
                    }

                    //SubFlowToSession correspondingSession = new SubFlowToSession(stmt, discoveredSession);
                } else if(((InitiatingSubFlow) stmt).getSubFlowVariableName().isPresent()) { //called with a variable as
                    //a parameter (e.g. subFlow(previouslyDeclaredSubFlowName))
                    String targetFlowName = ((InitiatingSubFlow) stmt).getSubFlowVariableName().get();
                    if (!nameToSubFlowMap.containsKey(targetFlowName)) {
                        //no name was in the map -> this means it is probably a field or a parameter
                        discoveredSession = new Session(stmt); //we absolutely don't know to which session
                        //this subflow is linked, it was probably initiated in another piece of code
                        nameToSubFlowMap.put(targetFlowName, new SubFlowToSession(stmt, discoveredSession));
                    }
                    else {
                        final SubFlowToSession subFlowToSession = nameToSubFlowMap.get(targetFlowName);
                        discoveredSession = subFlowToSession.getPointedSession();
                    }
                }
            }

            if(sessionToStatementMap.containsKey(discoveredSession)) {
                sessionToStatementMap.get(discoveredSession).add(stmt);
            }
            else {
                List<StatementInterface> newList = new LinkedList<>();
                newList.add(stmt);
                sessionToStatementMap.put(discoveredSession, newList);
            }


        });
    }

    public Map<Session, List<StatementInterface>> getSessionToStatementMap() {
        return sessionToStatementMap;
    }

    public static boolean isNullSession(Session session) {
        return session == nullSession;
    }

    public static boolean isNoSessionSession(Session session) {
        return session == noSessionSession;
    }

    public static boolean isValidSession(Session session) {
        return !isNullSession(session) && !isNoSessionSession(session);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nname to session map:\n");
        sb.append("{");

        sb.append(nameToSessionMap.entrySet().stream()
                .map(Map.Entry::toString)
                .collect(Collectors.joining(", ")));

        sb.append("}\n");

        sb.append("session to statement map:\n");

        sb.append("{ ");
        sessionToStatementMap.forEach((session, stmtList) -> {
            sb.append("[ ");

            if(session == nullSession) {
                sb.append("null session");
            } else {
                sb.append(session.initiatingStatement == null ?
                        "null statement" : session.initiatingStatement.getTargetSessionName().orElse("notFound"));
            }
            sb.append(" : ");
            sb.append(stmtList.stream().map(StatementInterface::toString).collect(Collectors.joining(", ")));
            sb.append(" ]\n");
        });
        sb.append(" } \n");

        return sb.toString();
    }
}