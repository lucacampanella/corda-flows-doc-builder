package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.*;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import net.corda.core.flows.FlowSession;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.template.TemplateMatcher;

import java.util.*;
import java.util.stream.Collectors;

public class StaticAnalyzerUtils {

    private StaticAnalyzerUtils() {
        //private constructor to hide public one
    }

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
        private final Session pointedSession;

        public SubFlowToSession(StatementInterface initiatingStatement, Session pointedSession) {
            this.initiatingStatement = initiatingStatement;
            this.pointedSession = pointedSession;
        }

        public Session getPointedSession() {
            return pointedSession;
        }
    }

    private static class CombinationWithSessions {
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

    public static CtMethod findCallMethod(CtClass klass) {
        List<CtMethod> call;
          //we look in the class and if it's not there in the superclasses
        CtClass currClass = klass;
        while(true) {
            call = currClass.getElements(new NamedElementFilter(CtMethod.class,
                    "call"));
            if(!call.isEmpty()) {
                return call.get(0);
            }
            try {
                currClass = (CtClass) currClass.getSuperclass().getTypeDeclaration();
                if(currClass == null) {
                    return null;
                }
            } catch (NullPointerException e) {
                return null; //we arrived at a point in the call stack where there is either no superclass
                //or it's not accessible in our model
            }
        }
    }

    public static List<CtClass> getAllWronglyDoubleAnnotatedClasses(List<CtClass> initiatingClasses) {
        List<CtClass> wronglyDoubleAnnotated = new ArrayList<>();

        for(CtClass subClass : initiatingClasses) {
            for(CtClass superClass : initiatingClasses) {
                if (subClass != superClass && subClass.isSubtypeOf(superClass.getReference())) {
                    wronglyDoubleAnnotated.add(subClass);
                    break;
                }
            }
        }

        return wronglyDoubleAnnotated;
    }

//    public static boolean checkTwoClassesAndBuildGraphs(CtClass initiatingClass, CtClass initiatedClass,
//                                                      String pathToOutFolder) {
//
//        LOGGER.trace(initiatedClass);
//
//        CtMethod callMethodInitiating = StaticAnalyzer.findCallMethod(initiatingClass);
//        if(callMethodInitiating == null) {//TODO: is this fine? Why is there the tag but then no method?
//            LOGGER.trace("Didn't find a call method in class " + initiatingClass.getSimpleName());
//            return false;
//        }
//
//        List<CtStatement> initiatingInterestingCalls =
//                MatcherHelper.findInterestingCtStatements(callMethodInitiating);
////        LOGGER.trace("Initiating class: " + initiatingClass.getQualifiedName());
////        for(int i = 0; i < initiatingInterestingCalls.size(); ++i) {
////            LOGGER.trace("[" + i + "] " + initiatingInterestingCalls.get(i));
////        }
//
//        CtMethod callMethodInitiated = StaticAnalyzer.findCallMethod(initiatedClass);
//        if(callMethodInitiated == null) { //TODO: is this fine? Why is there the tag but then no method?
//            LOGGER.trace("Didn't find a call method in class " + initiatedClass.getSimpleName());
//            return false;
//        }
//
//        List<CtStatement> initiatedInterestingCalls =
//                MatcherHelper.findInterestingCtStatements(callMethodInitiated);
////
//
//        final List<StatementInterface> statementsLeft = Utils.fromCtStatementsToBaseStatementsNoNulls(initiatingInterestingCalls);
//        final Branch branchLeft = statementsToBranch(statementsLeft);
//
//        final List<List<StatementInterface>> allCombinationsLeft = createAllCombinations(branchLeft.getStatements());
//
//        for(List<StatementInterface> comb : allCombinationsLeft) {
//            LOGGER.trace("**** New combination ****");
//            comb.forEach(LOGGER::trace);
//        }
//
//        final List<CombinationWithSessions> combinationWithSessionsLeft = allCombinationsLeft.stream()
//                .map(CombinationWithSessions::new).collect(Collectors.toList());
//
//        LOGGER.trace(combinationWithSessionsLeft);
//
//
//        final List<StatementInterface> statementsRight = Utils.fromCtStatementsToBaseStatementsNoNulls(initiatedInterestingCalls);
//        final Branch branchRight = statementsToBranch(statementsRight);
//
//        final List<List<StatementInterface>> allCombinationsRight = createAllCombinations(branchRight.getStatements());
//
//        final List<CombinationWithSessions> combinationWithSessionsRight = allCombinationsRight.stream()
//                .map(CombinationWithSessions::new).collect(Collectors.toList());
//
//        LOGGER.trace(combinationWithSessionsRight);
//
//        LOGGER.trace("\n**** Right ****\n");
//
//        for(List<StatementInterface> comb : allCombinationsRight) {
//            LOGGER.trace("**** New combination ****");
//            comb.forEach(LOGGER::trace);
//        }
//
//        boolean foundOneCombination = false;
//
//        for(CombinationWithSessions combLeft : combinationWithSessionsLeft) {
//            for(CombinationWithSessions combRight : combinationWithSessionsRight) {
//                for (Map.Entry<Session, List<StatementInterface>> entryLeft :
//                        combLeft.getSessionToStatementMap().entrySet()) {
//                    if (CombinationWithSessions.isValidSession(entryLeft.getKey())) {
//                        List<StatementInterface> stmtListLeft = entryLeft.getValue();
//                        for (Map.Entry<Session, List<StatementInterface>> entryRight :
//                                combRight.getSessionToStatementMap().entrySet()) {
//                            if (CombinationWithSessions.isValidSession(entryRight.getKey())) {
//                                List<StatementInterface> stmtListRight = entryRight.getValue();
//                                if(checkTwoCombinations(stmtListLeft, stmtListRight)) {
//                                    foundOneCombination = true;
//                                    buildLinkBetweenWorkingCombinations(stmtListLeft, stmtListRight);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if(foundOneCombination) {
//            LOGGER.trace("found one combination");
//            GGraphBuilder gb = new GGraphBuilder();
//            gb.addSession(initiatingClass.getQualifiedName(), branchLeft);
//            gb.addSession(initiatedClass.getQualifiedName(), branchRight);
//            try {
//                gb.drawToFile(pathToOutFolder + initiatingClass.getQualifiedName() + "_vs_"
//                        + initiatedClass.getQualifiedName() + ".svg");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            LOGGER.trace("No combination found");
//        }
//
//        return foundOneCombination;
//    }
//
//    private static void buildLinkBetweenWorkingCombinations(List<StatementInterface> combLeft,
//                                                            List<StatementInterface> combRight) {
//        Deque<StatementInterface> initiatingQueue = new LinkedList<>(combLeft);
//        Deque<StatementInterface> initiatedQueue = new LinkedList<>(combRight);
//
//        while(!initiatingQueue.isEmpty() || !initiatedQueue.isEmpty()) {
//            StatementInterface instrLeft = consumeUntilBlockingOrBranch(initiatingQueue);
//            StatementInterface instrRight = consumeUntilBlockingOrBranch(initiatedQueue);
//            if (instrLeft == null || instrRight == null) {
//                return;
//            }
//            if (instrLeft instanceof BranchingStatement) {
//                //if it has a blocking statement in the condition than we need to keep it in the stack and
//                //see if accepts the companion on the other side
//
//                BranchingStatement branchingStatement = ((BranchingStatement) instrLeft);
//                initiatingQueue.remove();
//
//                if(branchingStatement.hasBlockingStatementInCondition()) {
//                    initiatingQueue.addFirst(branchingStatement.getBlockingStatementInCondition());
//                }
//                continue;
//            }
//            if(instrRight instanceof BranchingStatement) {
//                initiatedQueue.remove();
//                BranchingStatement branchingStatement = ((BranchingStatement) instrRight);
//
//                if(branchingStatement.hasBlockingStatementInCondition()) {
//                    initiatedQueue.addFirst(branchingStatement.getBlockingStatementInCondition());
//                }
//                continue;
//            }
//
//            StatementWithCompanionInterface statementLeft = (StatementWithCompanionInterface) instrLeft;
//            StatementWithCompanionInterface statementRight = (StatementWithCompanionInterface) instrRight;
//
//            if(!(statementLeft instanceof SendAndReceive) || ((SendAndReceive) statementLeft).isSentConsumed()) {
//                initiatingQueue.remove(); //we remove the statement of the queue
//            }
//
//            if(!(statementRight instanceof SendAndReceive) || ((SendAndReceive) statementRight).isSentConsumed()) {
//                initiatedQueue.remove(); //we remove the statement of the queue
//            }
//
//            statementLeft.createGraphLink(statementRight);
//        }
//    }


//    private static boolean checkTwoCombinations(List<StatementInterface> combLeft,
//                                                        List<StatementInterface> combRight) {
//        Deque<StatementInterface> initiatingQueue = new LinkedList<>(combLeft);
//        Deque<StatementInterface> initiatedQueue = new LinkedList<>(combRight);
//
//        int i = 0;
//
//        while(!initiatingQueue.isEmpty() || !initiatedQueue.isEmpty()) {
//            StatementInterface instrLeft = consumeUntilBlockingOrBranch(initiatingQueue);
//            StatementInterface instrRight = consumeUntilBlockingOrBranch(initiatedQueue);
//            if (instrLeft == null && instrRight == null) {
//                return true;
//            }
//            if((instrLeft == null && instrRight != null) || (instrLeft != null && instrRight == null)) {
//                return false;
//            }
//            if (instrLeft instanceof BranchingStatement) {
//                //if it has a blocking statement in the condition than we need to keep it in the stack and
//                //see if accepts the companion on the other side
//
//                BranchingStatement branchingStatement = ((BranchingStatement) instrLeft);
//                initiatingQueue.remove();
//                if(branchingStatement.hasBlockingStatementInCondition()) {
//                    initiatingQueue.addFirst(branchingStatement.getBlockingStatementInCondition());
//                }
//                continue;
//            }
//            if(instrRight instanceof BranchingStatement) {
//                initiatedQueue.remove();
//                BranchingStatement branchingStatement = ((BranchingStatement) instrRight);
//                if(branchingStatement.hasBlockingStatementInCondition()) {
//                    initiatedQueue.addFirst(branchingStatement.getBlockingStatementInCondition());
//                }
//                continue;
//            }
//
//            StatementWithCompanionInterface statementLeft = (StatementWithCompanionInterface) instrLeft;
//            StatementWithCompanionInterface statementRight = (StatementWithCompanionInterface) instrRight;
//
//            LOGGER.trace("\n Round " + i++);
//            LOGGER.trace(statementLeft);
//            LOGGER.trace(statementRight);
//
//            if(!(statementLeft instanceof SendAndReceive) || ((SendAndReceive) statementLeft).isSentConsumed()) {
//                initiatingQueue.remove(); //we remove the statement of the queue
//            }
//
//            if(!(statementRight instanceof SendAndReceive) || ((SendAndReceive) statementRight).isSentConsumed()) {
//                initiatedQueue.remove(); //we remove the statement of the queue
//            }
//
//            if(!statementLeft.acceptCompanion(statementRight)) {
//                LOGGER.trace("**** ERROR in flow logic!");
//
//                return false;
//            }
//        }
//
//        return true;
//    }

    private static Branch statementsToBranch(List<StatementInterface> statements) {
        Branch result = new Branch();
        for(StatementInterface stmt : statements) {
            result.add(stmt.desugar());
        }

        return result;
    }


//    private static List<List<StatementInterface>> createAllCombinations(List<StatementInterface> instructions) {
//        List<List<StatementInterface>> result = new LinkedList<>();
//        result.add(new LinkedList<>());
//
//        for(StatementInterface instr : instructions) {
//            for(List<StatementInterface> branch : result) {
//                branch.add(instr);
//            }
//            if(instr instanceof BranchingStatement) {
//                List<List<StatementInterface>> insideResults = new LinkedList<>();
//                //also add empty branches because they mean the empty road can be taken
//                //but null means that branching statement only has one way (not really a branch there, only used for
//                //do-while here)
//                if(((BranchingStatement) instr).getBranchTrue() != null) {
//                    insideResults.addAll(createAllCombinations(((BranchingStatement) instr).getBranchTrue()
//                            .getStatements()));
//                }
//                if(((BranchingStatement) instr).getBranchFalse() != null) {
//                    insideResults.addAll(createAllCombinations(((BranchingStatement) instr).getBranchFalse()
//                            .getStatements()));
//                }
//                List<List<StatementInterface>> newResult = new LinkedList<>();
//                for(List<StatementInterface> currBranch : result) {
//                    for(List<StatementInterface> newBranch : insideResults) {
//                        List<StatementInterface> bothTogether = new LinkedList<>();
//                        bothTogether.addAll(currBranch);
//                        bothTogether.addAll(newBranch);
//                        newResult.add(bothTogether);
//                    }
//                }
//                result = newResult;
//            }
//        }
//
//        return result;
//    }

    static StatementInterface consumeUntilBlockingOrBranch(Queue<StatementInterface> queue) {
        while(!queue.isEmpty()) {
            StatementInterface statement = queue.peek();
            if (statement.needsCompanion() || statement.isBranchingStatement()) {
                return statement;
            }
            queue.remove();
        }

        return null; //we return null if we consumed all the queue without finding aa blocking statement
    }

    public static Optional<String> findTargetSessionName(CtStatement ctStatement) {
        final List<CtVariableRead> mentionedSessions = ctStatement.
                getElements(new TypeFilter<>(CtVariableRead.class))
                .stream().filter(varRead ->
                        varRead.getType().box().getQualifiedName().equals(FlowSession.class.getCanonicalName()))
                .collect(Collectors.toList());

        if(!mentionedSessions.isEmpty()) {
            return Optional.ofNullable(mentionedSessions.get(0).toString());
        }
        return Optional.empty();
    }

    public static Branch getAllRelevantMethodInvocations(CtElement statement, AnalyzerWithModel analyzer) {
        Branch res = new Branch();

        if(statement instanceof CtAbstractInvocation) { //it's a method call
            CtAbstractInvocation inv = (CtAbstractInvocation) statement;
            if(!isCordaMethod(inv)) {
                MethodInvocation methodInvocation = MethodInvocation.fromCtStatement((CtStatement) inv, analyzer);
                res.addIfRelevant(methodInvocation);
                return res;
            }
        }
        if(!(statement instanceof CtLiteral)) {
            final List<CtElement> directChildren = statement.getDirectChildren();

            for (CtElement elem : directChildren) {
                res.addIfRelevant(getAllRelevantMethodInvocations(elem, analyzer));
            }
        }

        return res;
    }

    public static boolean isCordaMethod(CtAbstractInvocation element) {
        boolean matched = false;
        final Set<TemplateMatcher> allMatchers = MatcherHelper.getAllMatchers();
        for(TemplateMatcher matcher : allMatchers) {
            if(matcher.matches(element)) {
                matched = true;
                break;
            }
        }
        return matched;
    }
}
