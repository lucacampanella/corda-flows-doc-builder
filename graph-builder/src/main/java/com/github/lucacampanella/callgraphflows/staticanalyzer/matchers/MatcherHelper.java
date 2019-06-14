package com.github.lucacampanella.callgraphflows.staticanalyzer.matchers;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.*;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.compiler.VirtualFile;
import spoon.template.TemplateMatcher;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class that handles patterns
 */
public final class MatcherHelper {

    private static final Map<String, TemplateMatcher> matchersMap = new HashMap<>();

    private static final Map<Class, CtTypeReference> typesMap = new HashMap<>();

    private static CtModel model;

    private static Set<String> allMatchersName = null;

    private static final String[] matchersWithCompanion = {"sendMatcher", "sendWithBoolMatcher", "receiveMatcher",
            "receiveWithBoolMatcher", "sendAndReceiveMatcher", "sendAndReceiveWithBoolMatcher", "subFlowMatcher"};

    static {
        Launcher launcher = new Launcher();

        //adds the jars needed to understand the corda imports


//        final InputStream dependenciesTxt = MatcherHelper.class.getClassLoader().getResourceAsStream("AllPossibleClassPaths.txt");
//        final String[] paths = new BufferedReader(new InputStreamReader(dependenciesTxt)).lines()
//                .filter(str -> str.contains(".jar"))
//                .toArray(String[]::new);
//
//        launcher.getEnvironment().setSourceClasspath(paths);

        final InputStream matcherContainerStream =
                MatcherHelper.class.getClassLoader().getResourceAsStream("MatcherContainer.java");

        if(matcherContainerStream == null) {
            throw new RuntimeException("MatcherContainer.java not found");
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
            CtMethod<?> method = (CtMethod<?>) model.getElements(
                    new NamedElementFilter(CtMethod.class, name)).get(0);
            CtElement templateRoot = method.getBody().getStatement(0);
            return new TemplateMatcher(templateRoot);
        });
    }

    public static CtTypeReference getTypeReference(Class klass) {
        return typesMap.computeIfAbsent(klass, key -> {
            CtMethod<?> method = (CtMethod<?>) model.getElements(
                    new NamedElementFilter(CtMethod.class, "typeTemplateFor" + klass.getSimpleName())).get(0);

            return  ((CtLocalVariable) method.getBody().getStatement(0)).getType();
        });
    }

    /**
     * Queries a statement for the matcher indicated by the matcherName and returns the corresponding expression
     * @param statement the statement to query
     * @param matcherName the name of the matcher to use in the query
     * @return the matched expression
     */
    public static CtExpression getFirstMatchedExpression(CtStatement statement, String matcherName) {
        final List<CtExpression> queryResult = statement.filterChildren(getMatcher(matcherName)).list();
        if(queryResult.isEmpty()) {
            return null;
        }
        return queryResult.get(0);
    }

    public static CtExpression getFirstMatchedStatementWithCompanion(CtQueryable statement) {
        for(String matcherName : matchersWithCompanion) {
            final List<CtExpression> queryResult = statement.filterChildren(getMatcher(matcherName)).list();
            if (!queryResult.isEmpty()) {
                return queryResult.get(0);
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
    public static boolean matchesAnyChildren(CtQueryable queryable, String matcherName) {
        return !queryable.filterChildren(getMatcher(matcherName)).list().isEmpty();
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
        } else if (matchesAnyChildren(statement, "sendMatcher") ||
                matchesAnyChildren(statement, "sendWithBoolMatcher")) { //TODO: test the second line
            return Send.fromCtStatement(statement, analyzer);
        } else if (matchesAnyChildren(statement, "receiveMatcher") ||
                matchesAnyChildren(statement, "receiveWithBoolMatcher")) { //TODO: test the second line
            return Receive.fromCtStatement(statement, analyzer);
        } else if (matchesAnyChildren(statement, "sendAndReceiveMatcher") ||
                matchesAnyChildren(statement, "sendAndReceiveWithBoolMatcher")) { //TODO: test the second line
            return SendAndReceive.fromCtStatement(statement, analyzer);
        }
        else if (matchesAnyChildren(statement, "subFlowMatcher")) {
            return SubFlowBuilder.fromCtStatement(statement, analyzer);
        }

        return null;
    }

    private static StatementInterface initiateIfTypedElementContainsFlowSessionOrFlowLogic(CtStatement statement,
                                                                                           AnalyzerWithModel analyzer) {
        CtTypedElement elem = (CtTypedElement) statement;
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
        if(statementWithRelevantMethods.isRelevantForAnalysis())
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
        if(res == null) {
            res = initiateIfContainsRelevantMethod(statement, analyzer);
        }

        return res;
    }

    public static Branch fromCtStatementsToStatements(List<CtStatement> ctStatements, AnalyzerWithModel analyzer) {

        final Branch res = new Branch();

        for(CtStatement ctStatement : ctStatements) {
            //here we desugar already!
            final StatementInterface statement = instantiateStatement(ctStatement, analyzer);
            if(statement != null) {
                Branch desugared = statement.desugar();
                res.addIfRelevant(desugared);
            }
        }
        return res;
    }

    public static List<CtStatement> findInterestingCtStatements(CtMethod method) {

        Set<TemplateMatcher> allMatchers = getAllMatchers();

        List<CtStatement> result = new ArrayList<>();

        List<CtStatement> statements = method.getBody().getStatements();

        boolean matched = false;
        for(CtStatement statement : statements) {
            matched = false;
            for (TemplateMatcher matcher : allMatchers) {
                if (!statement.filterChildren(matcher).list().isEmpty()) {
                    result.add(statement);
                    matched = true;
                    break;
                }
            }
//            if(!matched && statement instanceof CtAbstractInvocation) {
//                result.add(statement);
//                matched = true;
//            }
            final List<CtLocalVariable> localVars = statement.getElements(new TypeFilter<>(CtLocalVariable.class));
            if (!matched && !localVars.isEmpty()) {
                for (CtLocalVariable localVariable : localVars) {
                    if (localVariable.getType().isSubtypeOf(getTypeReference(FlowSession.class))
                            || localVariable.getType().isSubtypeOf(getTypeReference(FlowLogic.class))) {
                        result.add(statement);
                        matched = true;
                        break;
                    }
                }
            }
            final List<CtAssignment> assigments = statement.getElements(new TypeFilter<>(CtAssignment.class));
            if (!matched && !assigments.isEmpty()) {
                for (CtAssignment assignment : assigments) {
                    if (assignment.getType().isSubtypeOf(getTypeReference(FlowSession.class))
                            || assignment.getType().isSubtypeOf(getTypeReference(FlowLogic.class))) {
                        result.add(statement);
                        matched = true;
                        break;
                    }
                }
            }
        }
        return result;
    }



    public static StatementInterface instantiateStatementIfQueryableMatches(CtQueryable queryable,
                                                                                         CtStatement statement,
                                                                                         AnalyzerWithModel analyzer) {
        if (matchesAnyChildren(queryable, "sendMatcher") ||
                matchesAnyChildren(statement, "sendWithBoolMatcher")) { //TODO: test the second line
            return Send.fromCtStatement(statement, analyzer);
        } else if (matchesAnyChildren(queryable, "receiveMatcher") ||
                matchesAnyChildren(statement, "receiveWithBoolMatcher")) { //TODO: test the second line
            return Receive.fromCtStatement(statement, analyzer);
        } else if (matchesAnyChildren(queryable, "sendAndReceiveMatcher") ||
                matchesAnyChildren(statement, "sendAndReceiveWithBoolMatcher")) { //TODO: test the second line
            return SendAndReceive.fromCtStatement(statement, analyzer);
        }
        else if (matchesAnyChildren(queryable, "subFlowMatcher")) {
            return SubFlowBuilder.fromCtStatement(statement, analyzer);
        }
        return null;
    }
}
