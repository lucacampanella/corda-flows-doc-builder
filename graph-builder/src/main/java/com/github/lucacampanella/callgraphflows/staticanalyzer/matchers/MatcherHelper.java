package com.github.lucacampanella.callgraphflows.staticanalyzer.matchers;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.*;
import com.github.lucacampanella.callgraphflows.utils.Utils;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.pattern.Pattern;
import spoon.pattern.PatternBuilder;
import spoon.pattern.PatternBuilderHelper;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.compiler.VirtualFile;
import spoon.template.TemplateMatcher;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Helper class that handles patterns
 */
public final class MatcherHelper {

    private static final Map<String, TemplateMatcher> matchersMap = new HashMap<>();

    private static final Map<Class, CtTypeReference> typesMap = new HashMap<>();

    private static CtModel model;

    private static Set<String> allMatchersName = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherHelper.class);

    private static final String SEND_MATCHER = "sendMatcher";
    private static final String SEND_WITH_BOOL_MATCHER = "sendWithBoolMatcher";
    private static final String RECEIVE_MATCHER = "receiveMatcher";
    private static final String RECEIVE_WITH_BOOL_MATCHER = "receiveWithBoolMatcher";
    private static final String SEND_AND_RECEIVE_MATCHER = "sendAndReceiveMatcher";
    private static final String SEND_AND_RECEIVE_WITH_BOOL_MATCHER = "sendAndReceiveWithBoolMatcher";
    private static final String SUB_FLOW_MATCHER = "subFlowMatcher";

    private static final String[] matchersWithCompanion = {SEND_MATCHER, SEND_WITH_BOOL_MATCHER, RECEIVE_MATCHER,
            RECEIVE_WITH_BOOL_MATCHER, SEND_AND_RECEIVE_MATCHER, SEND_AND_RECEIVE_WITH_BOOL_MATCHER, SUB_FLOW_MATCHER};

    static {
        Launcher launcher = new Launcher();

        final InputStream matcherContainerStream =
                MatcherHelper.class.getClassLoader().getResourceAsStream("MatcherContainer.java");

        if(matcherContainerStream == null) {
            throw new MatcherException("MatcherContainer.java not found");
        }

        final String matcherContainerString = new BufferedReader(new InputStreamReader(matcherContainerStream)).lines()
                .parallel().collect(Collectors.joining("\n"));

        final VirtualFile matcherContainerVirtualFile = new VirtualFile(matcherContainerString);

        launcher.addInputResource(matcherContainerVirtualFile);
        launcher.buildModel();
        model = launcher.getModel();
    }

    private MatcherHelper() {
        //private constructor to hide public one
    }

    /**
     * Creates or loads the matcher corresponding to the passed name
     * @param name name of the matcher, so the name of the method containing the matcher
     * @return the matcher corresponding to the first line of the method with that name in {@link MatcherContainer}
     */
    public static TemplateMatcher getMatcher(String name) {
        return matchersMap.computeIfAbsent(name, key -> {
            CtElement templateRoot = getFirstLineOfMethod(name);
            return new TemplateMatcher(templateRoot);
        });
    }

    private static CtElement getFirstLineOfMethod(String methodName) {
        CtMethod<?> method = (CtMethod<?>) model.getElements(
                new NamedElementFilter(CtMethod.class, methodName)).get(0);
        return method.getBody().getStatement(0);
    }

    public static CtTypeReference getTypeReference(Class klass) {
        return typesMap.computeIfAbsent(klass, key -> {
            CtMethod<?> method = (CtMethod<?>) model.getElements(
                    new NamedElementFilter(CtMethod.class, "typeTemplateFor" +
                            Utils.removePackageDescription(klass.getName()))).get(0);

            return  ((CtLocalVariable) method.getBody().getStatement(0)).getType();
        });
    }

    /**
     * Queries a statement for the matcher indicated by the matcherName and returns the corresponding expression
     * @param statement the statement to query
     * @param matcherName the name of the matcher to use in the query
     * @return the matched expression
     */
    public static CtAbstractInvocation getFirstMatchedExpression(CtElement statement, String matcherName) {

        Queue<CtElement> queue = new LinkedList<>();
        queue.add(statement);

        //breadth first search
        while(!queue.isEmpty()){
            CtElement current = queue.remove();
            if(current == null) {
                continue;
            }
            if(current instanceof CtAbstractInvocation) {
                CtAbstractInvocation inv = (CtAbstractInvocation) current;
                if(invocationMatches(inv, matcherName)) {
                    return inv;
                }
            }
            queue.addAll(current.getDirectChildren());
        }

        return null;
    }

    private static String getSignatureDescription(String matcherName) {
        final CtElement firstLineOfMethod = getFirstLineOfMethod(matcherName);
        final CtAbstractInvocation ctAbstractInvocation =
                firstLineOfMethod.getElements(new TypeFilter<>(CtAbstractInvocation.class)).get(0);
        return ctAbstractInvocation.getExecutable().getSignature();
    }

    public static boolean invocationMatches(CtAbstractInvocation inv, String matcherName) {
        return inv.getExecutable().getSignature().equals(getSignatureDescription(matcherName));
    }

    public static CtAbstractInvocation getFirstMatchedStatementWithCompanion(CtElement statement) {
        for(String matcherName : matchersWithCompanion) {
            final CtAbstractInvocation inv = getFirstMatchedExpression(statement, matcherName);
            if(inv != null) {
                return inv;
            }
        }
        return null;
    }

    /**
     * Returns all the possible matcher names, inferred from the names of {@link MatcherContainer} with reflection
     * @return all the possible matcher names
     */
    public static Set<String> getAllMatcherNames() {
        if(allMatchersName == null) {
            allMatchersName = new HashSet<>();
            Class c = MatcherContainer.class;
            Method[] methods = c.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().endsWith("Matcher")) {
                    allMatchersName.add(m.getName());
                }
            }
        }

        return allMatchersName;
    }

    /**
     * returns all the matchers declared in {@link MatcherContainer}
     * @return all the matchers
     */
    public static Set<TemplateMatcher> getAllMatchers() {
        return getAllMatcherNames().stream().map(MatcherHelper::getMatcher).collect(Collectors.toSet());
    }

    /**
     * Checks if a queryable matchesAnyChildren a matcher
     * @param queryable the queryable to query
     * @param matcherName the name of the matcher to use in the query
     * @return true if the matcher finds a result inside the queryable, false otherwise
     */
    public static boolean matchesAnyChildren(CtElement queryable, String matcherName) {
        return getFirstMatchedExpression(queryable, matcherName) != null;
    }

    private static StatementInterface addIfBranchingStatement(CtStatement statement,
                                                              AnalyzerWithModel analyzer) {
        if(statement instanceof CtIf) {
            return IfElse.fromCtStatement(statement, analyzer);
        }
        else if(statement instanceof CtWhile) {
            return While.fromStatement(statement, analyzer);
        }
        else if(statement instanceof CtFor) {
            return For.fromCtStatement(statement, analyzer);
        }
        else if(statement instanceof CtForEach) {
            return ForEach.fromCtStatement(statement, analyzer);
        }
        else if(statement instanceof CtDo) {
            //this will later be desugared into a while
            return DoWhile.fromCtStatement(statement, analyzer);
        }

        return null;
    }

    private static StatementInterface initiateIfCordaRelevantStatement(CtStatement statement,
                                                            AnalyzerWithModel analyzer) {
        if (matchesAnyChildren(statement, "transactionBuilderMatcher")) {
            return TransactionBuilder.fromStatement(statement, analyzer);
        } else if (matchesAnyChildren(statement, "initiateFlowMatcher")) {
            return InitiateFlow.fromCtStatement(statement, analyzer);
        } else if (matchesAnyChildren(statement, SEND_MATCHER) ||
                matchesAnyChildren(statement, SEND_WITH_BOOL_MATCHER)) {
            return Send.fromCtStatement(statement, analyzer);
        } else if (matchesAnyChildren(statement, RECEIVE_MATCHER) ||
                matchesAnyChildren(statement, RECEIVE_WITH_BOOL_MATCHER)) {
            return Receive.fromCtStatement(statement, analyzer);
        } else if (matchesAnyChildren(statement, SEND_AND_RECEIVE_MATCHER) ||
                matchesAnyChildren(statement, SEND_AND_RECEIVE_WITH_BOOL_MATCHER)) {
            return SendAndReceive.fromCtStatement(statement, analyzer);
        }
        else if (matchesAnyChildren(statement, SUB_FLOW_MATCHER)) {
            return SubFlowBuilder.fromCtStatement(statement, analyzer);
        }

        return null;
    }

    private static StatementInterface initiateIfTypedElementContainsFlowSessionOrFlowLogic(CtStatement statement,
                                                                                           AnalyzerWithModel analyzer) {
        CtTypedElement elem = (CtTypedElement) statement;
        if(elem.getType() == null) {
            LOGGER.warn("Couldn't get type of {}, continuing without trying to figure out " +
                    "if containts a flow session or a flow logic type"
                    + "\nThis could result in a problem in the produced graph", statement);
            return null;
        }

        if(elem.getType().isSubtypeOf(getTypeReference(FlowSession.class))) {
            return SessionAssignment.fromCtStatement(statement, analyzer);
        }
        else if (elem.getType().isSubtypeOf(getTypeReference(FlowLogic.class))) {
            if(!statement.getElements(new TypeFilter<>(CtAbstractInvocation.class)).isEmpty()) {
                return FlowConstructor.fromStatement(statement, analyzer);
            }
            else {
                return FlowAssignment.fromCtStatement(statement, analyzer);
            }
        }

        return null;
    }

    private static StatementInterface initiateIfContainsRelevantMethod(CtStatement statement, AnalyzerWithModel analyzer) {
        final List<CtAbstractInvocation> methods = statement.getElements(new TypeFilter<>(CtAbstractInvocation.class));
        if(methods.isEmpty()) {
            return null;
        }
        final StatementWithRelevantMethods statementWithRelevantMethods =
                StatementWithRelevantMethods.fromCtStatement(statement, analyzer);
        if(statementWithRelevantMethods.isRelevantForLoopFlowBreakAnalysis())
            return statementWithRelevantMethods;
        return null;
    }

    public static StatementInterface instantiateStatement(CtStatement statement, AnalyzerWithModel analyzer) {

        StatementInterface res = addIfBranchingStatement(statement, analyzer);

        if(res == null) {
            res = initiateIfCordaRelevantStatement(statement, analyzer);
        }
        if(res == null && (statement instanceof CtAssignment || statement instanceof CtLocalVariable)) {
            res = initiateIfTypedElementContainsFlowSessionOrFlowLogic(statement, analyzer);
        }
        if(res == null && statement instanceof CtAbstractInvocation) {
            res = MethodInvocation.fromCtStatement(statement, analyzer);
        }
        if(res == null && statement instanceof CtCFlowBreak) {
            res = CodeFlowBreak.fromStatement(statement,analyzer);
        }
        if(res == null) {
            res = initiateIfContainsRelevantMethod(statement, analyzer);
        }

        return res;
    }

    public static Branch fromCtStatementsToStatements(List<CtStatement> ctStatements,
                                                      AnalyzerWithModel analyzer) {
        final Branch res = new Branch();

        for(CtStatement ctStatement : ctStatements) {
            //here we desugar already!
            final StatementInterface statement = instantiateStatement(ctStatement, analyzer);
            if(statement != null) {
                Branch desugared = statement.desugar();
                res.addIfRelevantForLoopFlowBreakAnalysis(desugared);
            }
        }
        return res;
    }

    public static StatementInterface instantiateStatementIfQueryableMatches(CtElement queryable,
                                                                                         CtStatement statement,
                                                                                         AnalyzerWithModel analyzer) {
        if (matchesAnyChildren(queryable, SEND_MATCHER) ||
                matchesAnyChildren(statement, SEND_WITH_BOOL_MATCHER)) {
            return Send.fromCtStatement(statement, analyzer);
        } else if (matchesAnyChildren(queryable, RECEIVE_MATCHER) ||
                matchesAnyChildren(statement, RECEIVE_WITH_BOOL_MATCHER)) {
            return Receive.fromCtStatement(statement, analyzer);
        } else if (matchesAnyChildren(queryable, SEND_AND_RECEIVE_MATCHER) ||
                matchesAnyChildren(statement, SEND_AND_RECEIVE_WITH_BOOL_MATCHER)) {
            return SendAndReceive.fromCtStatement(statement, analyzer);
        }
        else if (matchesAnyChildren(queryable, SUB_FLOW_MATCHER)) {
            return SubFlowBuilder.fromCtStatement(statement, analyzer);
        }

        return null;
    }

    public static boolean isCordaMethod(CtAbstractInvocation element) {
        final Set<String> allMatchers = getAllMatcherNames();
        for(String matcherName : allMatchers) {
            if(invocationMatches(element, matcherName)) {
                return true;
            }
        }
        return false;
    }

    //OLD SYSTEM WITH SPOON DOING THE QUERIES
    //    public static boolean matchesAnyChildren(CtQueryable queryable, String matcherName) {
//        //return !queryable.filterChildren(getMatcher(matcherName)).list().isEmpty();
//    }
//    public static boolean matchesAnyChildren(CtQueryable queryable, String matcherName) {
//        return !queryable.filterChildren(getMatcher(matcherName)).list().isEmpty();
//    }
//    public static boolean isCordaMethod(CtAbstractInvocation element) {
//        boolean matched = false;
//        final Set<TemplateMatcher> allMatchers = getAllMatchers();
//        for(TemplateMatcher matcher : allMatchers) {
//            if(matcher.matches(element)) {
//                matched = true;
//                break;
//            }
//        }
//        return matched;
//    }
//    public static CtExpression getFirstMatchedExpression(CtStatement statement, String matcherName) {
//        final List<CtExpression> queryResult = statement.filterChildren(getMatcher(matcherName)).list();
//        if(queryResult.isEmpty()) {
//            return null;
//        }
//        return queryResult.get(0);
//    }
//    public static CtExpression getFirstMatchedStatementWithCompanion(CtQueryable statement) {
//        for(String matcherName : matchersWithCompanion) {
//            final List<CtExpression> queryResult = statement.filterChildren(getMatcher(matcherName)).list();
//            if (!queryResult.isEmpty()) {
//                return queryResult.get(0);
//            }
//        }
//        return null;
//    }
}
