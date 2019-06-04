package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.graphics.components.GGraphBuilder;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import net.corda.core.flows.InitiatedBy;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.path.CtPath;
import spoon.reflect.path.CtPathStringBuilder;
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtFieldReadImpl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyzerWithModel {
    protected CtModel model;

    public CtModel getModel() {
        return model;
    }

    public <T> CtClass<T> getClass(Class<T> klass) {
        final List<CtClass> results = model.getElements(new NamedElementFilter(CtClass.class, klass.getSimpleName()));
        if(results.isEmpty()) {
            return null;
        }
        return (CtClass<T>) results.get(0);
    }


    public AnalysisResult analyzeFlowLogicClass(CtClass klass) {
        System.out.println("*** analyzing sub-class " + klass.getQualifiedName());
        final CtMethod callMethod = StaticAnalyzerUtils.findCallMethod(klass);
        if(callMethod == null) {
            return null;
        }

        AnalysisResult res = new AnalysisResult(klass.getSimpleName());
        final Branch interestingStatements = MatcherHelper.fromCtStatementsToStatements(
                callMethod.getBody().getStatements(), this);
        res.setStatements(interestingStatements);

        //is it only a "container" flow with no initiating call or also calls initiateFlow(...)?
        final boolean isInitiatingFlow =
                interestingStatements.getInitiateFlowStatementAtThisLevel().isPresent();

        System.out.println("Contains initiate call? " + isInitiatingFlow);
        if(isInitiatingFlow) {
            CtClass initiatedFlowClass = getDeeperClassInitiatedBy(klass);

            if(initiatedFlowClass != null) {
                res.setCounterpartyClassResult(analyzeFlowLogicClass(initiatedFlowClass));
            }
        }

        return res;
    }

    //    ** A new responder written to override an existing responder must _still_ be annotated with `@InitiatedBy`
    //    referencing the base initiator.
    //    ** A new initiator written to override an existing initiator must _not_ have the @InitiatingFlow annotation.

    //todo: better way to find the classes to analyze in a jar, also classes with no corresponding "initiated"
    // flow should be analyzed
    public Map<CtClass, CtClass> getInitiatedClassToInitiatingMap() {

        //find all initiating classes inside the jar and keep only the ones in the extention graph that are
        //furthest away from the base class

//        List<CtClass> initiatingClasses = getClassesByAnnotation(InitiatingFlow.class);
//
//        List<CtClass> wrongDoubleAnnotatedClasses = StaticAnalyzer.getAllWronglyDoubleAnnotatedClasses(initiatingClasses);
//
//        wrongDoubleAnnotatedClasses.forEach(klass -> System.out.println("*** WARNING: the class " +
//                klass.getSimpleName() + " contains the annotation @InitiatingFlow, but is already subclass" +
//                " of a class containing such annotation, please remove it."));
//
//        initiatingClasses.removeAll(wrongDoubleAnnotatedClasses);
//
//        Map<CtClass, CtClass> superClassToFurthestAway = new HashMap<>(initiatingClasses.size());
//        for(CtClass klass : initiatingClasses) {
//            superClassToFurthestAway.put(klass, getFurthestAwaySubclass(klass));
//        }

        //find all classes that are initiated by flows
        List<CtClass> initiatedClasses = getClassesByAnnotation(InitiatedBy.class);

        //retain only the ones that are furthest away from FlowLogic.class, these are the one that will actually
        // be run by corda
        Set<CtClass> furthestInitiatedClasses = new HashSet<>();
        initiatedClasses.forEach(klass -> furthestInitiatedClasses.add(getFurthestAwaySubclass(klass)));

        //map them to the class that initiates them
        Map<CtClass, CtClass> initiatedClassToInitiating = new HashMap<>(initiatedClasses.size());

        for(CtClass klass : furthestInitiatedClasses) {
            CtAnnotation initiatedByAnnotation = klass.getAnnotations().stream().filter(ctAnnotation ->
                    ctAnnotation.getActualAnnotation().annotationType() == InitiatedBy.class).findFirst().get();

            final CtExpression referenceToClass = (CtExpression) initiatedByAnnotation.getAllValues().get("value");

            //maybe not the best way to get the class, but the only one I found
            CtPath path = new CtPathStringBuilder().fromString("." +
                    ((CtFieldReadImpl) referenceToClass).getTarget().toString());

            final CtClass initiatingClass = (CtClass) path.evaluateOn(model.getRootPackage()).get(0);

            initiatedClassToInitiating.put(klass, initiatingClass);

        }

        return initiatedClassToInitiating;
    }

    public List<CtClass> getClassesByAnnotation(Class annotationClass) {
        List<CtElement> elements = model.getElements(new AnnotationFilter<>(annotationClass));

        return  elements.stream()
                .map(CtClass.class::cast)
                .collect(Collectors.toList());
    }

    public CtClass getDeeperClassInitiatedBy(CtClass initiatingClass) {
        CtClass deeperInitiatedByClass = null;
        final List<CtClass> generalInitiatedByList = getClassesByAnnotation(InitiatedBy.class);
        for(CtClass klass : generalInitiatedByList) {
            CtAnnotation initiatedByAnnotation = klass.getAnnotations().stream().filter(ctAnnotation ->
                    ctAnnotation.getActualAnnotation().annotationType() == InitiatedBy.class).findFirst().get();
            final CtExpression referenceToClass = (CtExpression) initiatedByAnnotation.getAllValues().get("value");

            final CtClass correspondingInitiatingClass = (CtClass) ((CtFieldReadImpl) referenceToClass).getVariable()
                    .getDeclaringType().getTypeDeclaration();

            if(correspondingInitiatingClass.getReference().isSubtypeOf(initiatingClass.getReference()) &&
                (deeperInitiatedByClass == null ||
                        klass.getReference().isSubtypeOf(deeperInitiatedByClass.getReference()))) {
                    deeperInitiatedByClass = klass;
                }

        }

        return deeperInitiatedByClass;
    }

    public CtClass getFurthestAwaySubclass(CtClass superClass) {
        List<CtClass> allClasses = model.getElements(new TypeFilter<>(CtClass.class));

        CtClass furthestAway = superClass;

        for(CtClass subClass : allClasses) {
            if (subClass.isSubtypeOf(superClass.getReference())
                    && subClass.isSubtypeOf(furthestAway.getReference())) {
                furthestAway = subClass;
            }
        }

        return furthestAway;
    }

    public List<CtClass> getAllSubClassesIncludingThis(CtClass superClass) {
        List<CtClass> allClasses = model.getElements(new TypeFilter<>(CtClass.class));

        return allClasses.stream().filter(klass -> klass.isSubtypeOf(superClass.getReference()))
                .collect(Collectors.toList());
    }
}
