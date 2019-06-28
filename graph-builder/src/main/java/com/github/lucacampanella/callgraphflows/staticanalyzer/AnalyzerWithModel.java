package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.AnalysisErrorException;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.StartableByRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyzerWithModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerWithModel.class);

    protected CtModel model;
    protected String analysisName;
    protected ClassCallStackHolder currClassCallStackHolder = null;

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


    public AnalysisResult analyzeFlowLogicClass(CtClass klass) throws AnalysisErrorException {
        LOGGER.info("*** analyzing sub-class {}", klass.getQualifiedName());
        final CtMethod callMethod = StaticAnalyzerUtils.findCallMethod(klass);
        if(callMethod == null) {
            throw new AnalysisErrorException(klass, "No call method found");
        }
        if(callMethod.isAbstract()) {
            String exMessage = "Found only an abstract call method";
            if(callMethod.getParent() instanceof CtClass) {
                exMessage += " in class " + ((CtClass) (callMethod).getParent()).getQualifiedName();
            }
            throw new AnalysisErrorException(klass, exMessage);
        }

        setCurrentAnalyzingClass(klass);

        AnalysisResult res = new AnalysisResult(ClassDescriptionContainer.fromClass(klass));

        final Branch interestingStatements = MatcherHelper.fromCtStatementsToStatements(
                callMethod.getBody().getStatements(), this);
        res.setStatements(interestingStatements);

        //is it only a "container" flow with no initiating call or also calls initiateFlow(...)?
        final boolean isInitiatingFlow =
                interestingStatements.getInitiateFlowStatementAtThisLevel().isPresent();

        LOGGER.debug("Contains initiate call? {}", isInitiatingFlow);
        if(isInitiatingFlow) {
            CtClass initiatedFlowClass = getDeeperClassInitiatedBy(klass);

            if(initiatedFlowClass != null) {
                res.setCounterpartyClassResult(analyzeFlowLogicClass(initiatedFlowClass));
            }
        }

        return res;
    }

    public void setCurrentAnalyzingClass(CtClass<?> klass) {
        currClassCallStackHolder = ClassCallStackHolder.fromCtClass(klass);
    }

    public ClassCallStackHolder getCurrClassCallStackHolder() {
        return currClassCallStackHolder;
    }

    //    ** A new responder written to override an existing responder must _still_ be annotated with `@InitiatedBy`
    //    referencing the base initiator.
    //    ** A new initiator written to override an existing initiator must _not_ have the @InitiatingFlow annotation.
//    public Map<CtClass, CtClass> getInitiatedClassToInitiatingMap() {
//
//        //find all classes that are initiated by flows
//        List<CtClass> initiatedClasses = getClassesByAnnotation(InitiatedBy.class);
//
//        //retain only the ones that are furthest away from FlowLogic.class, these are the one that will actually
//        // be run by corda
//        Set<CtClass> furthestInitiatedClasses = new HashSet<>();
//        initiatedClasses.forEach(klass -> furthestInitiatedClasses.add(getFurthestAwaySubclass(klass)));
//
//        //map them to the class that initiates them
//        Map<CtClass, CtClass> initiatedClassToInitiating = new HashMap<>(initiatedClasses.size());
//
//        for(CtClass klass : furthestInitiatedClasses) {
//            final Optional<CtAnnotation<? extends Annotation>> initiatedByAnnotationOptional = klass.getAnnotations().stream()
//                    .filter(ctAnnotation ->
//                    ctAnnotation.getActualAnnotation().annotationType() == InitiatedBy.class).findFirst();
//
//            if(initiatedByAnnotationOptional.isPresent()) {
//                final CtAnnotation<? extends Annotation> initiatedByAnnotation = initiatedByAnnotationOptional.get();
//
//                final CtExpression referenceToClass = (CtExpression) initiatedByAnnotation.getAllValues().get("value");
//
//                //maybe not the best way to get the class, but the only one I found
//                CtPath path = new CtPathStringBuilder().fromString("." +
//                        ((CtFieldReadImpl) referenceToClass).getTarget().toString());
//
//                final CtClass initiatingClass = (CtClass) path.evaluateOn(model.getRootPackage()).get(0);
//
//                initiatedClassToInitiating.put(klass, initiatingClass);
//            }
//
//        }
//
//        return initiatedClassToInitiating;
//    }

    public List<CtClass> getClassesByAnnotation(Class annotationClass) {
        List<CtElement> elements = model.getElements(new AnnotationFilter<>(annotationClass));

        return  elements.stream()
                .map(CtClass.class::cast)
                .collect(Collectors.toList());
    }

    public List<CtClass> getClassesToBeAnalyzed() {
        return getClassesByAnnotation(StartableByRPC.class);
    }


        public CtClass getDeeperClassInitiatedBy(CtClass initiatingClass) {
        CtClass deeperInitiatedByClass = null;
        final List<CtClass> generalInitiatedByList = getClassesByAnnotation(InitiatedBy.class);
        for(CtClass klass : generalInitiatedByList) {

            Optional<CtAnnotation<? extends Annotation>> initiatedByAnnotationOptional =
                    klass.getAnnotations().stream().filter(ctAnnotation -> {
                        boolean result = false;
                        try {
                            result = ctAnnotation.getActualAnnotation().annotationType() == InitiatedBy.class;
                        } catch (Exception e) {
                            LOGGER.warn("Couldn't retrieve real representation for annotation {} for class {}, " +
                                    "continuing without analyzing this one", ctAnnotation, klass.getQualifiedName());
                        }
                        return result;
                    }).findFirst();
            if(initiatedByAnnotationOptional.isPresent()) {
                final CtExpression referenceToClass = (CtExpression)
                        initiatedByAnnotationOptional.get().getAllValues().get("value");

                if(((CtFieldReadImpl) referenceToClass).getVariable().getDeclaringType() == null){
                    LOGGER.warn("Couldn't retrieve declaration of class declared in the @initiatedBy " +
                            "annotation. Skipping this class in finding the responder flow " +
                            "\nThis could result in a problem in the produced graph." +
                            " \nDeclared reference: {} \nDeclaring class: {} " +
                            "\nInitiatingClass {}", referenceToClass, klass, initiatingClass);
                    continue;
                }

                final CtClass correspondingInitiatingClass = (CtClass) ((CtFieldReadImpl) referenceToClass).getVariable()
                        .getDeclaringType().getTypeDeclaration();

                if ((correspondingInitiatingClass.getReference().isSubtypeOf(initiatingClass.getReference())
                || initiatingClass.getReference().isSubtypeOf(correspondingInitiatingClass.getReference())) &&
                        (deeperInitiatedByClass == null ||
                                klass.getReference().isSubtypeOf(deeperInitiatedByClass.getReference()))) {
                    deeperInitiatedByClass = klass;
                }
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

    public String getAnalysisName() {
        return analysisName;
    }
}
