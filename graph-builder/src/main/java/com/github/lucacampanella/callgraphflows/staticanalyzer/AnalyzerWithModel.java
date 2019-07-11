package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import net.corda.core.flows.FlowLogic;
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
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtFieldReadImpl;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyzerWithModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerWithModel.class);

    private static boolean drawArrows = true;

    protected CtModel model;
    protected String analysisName;
    protected ClassCallStackHolder currClassCallStackHolder = null;

    private Map<CtClass, AnalysisResult> classToAnalysisResultMap = new HashMap<>();

    public CtModel getModel() {
        return model;
    }

    public static void setDrawArrows(boolean drawArrows) {
        AnalyzerWithModel.drawArrows = drawArrows;
    }

    public <T> CtClass<T> getClass(Class<T> klass) {
        final List<CtClass> results = model.getElements(new NamedElementFilter(CtClass.class, klass.getSimpleName()));
        for(CtClass ctClass : results) {
            if(ctClass.getQualifiedName().equals(klass.getName())) {
                return (CtClass<T>) ctClass;
            }
        }
        return null;
    }

    public AnalysisResult analyzeFlowLogicClass(Class klass) throws AnalysisErrorException {
        final CtClass ctClass = getClass(klass);
        if(ctClass == null) {
            throw new IllegalArgumentException("The class is not in the model");
        }
        return analyzeFlowLogicClass(ctClass);
    }

    public AnalysisResult analyzeFlowLogicClass(CtClass klass) throws AnalysisErrorException {
        if(classToAnalysisResultMap.containsKey(klass)) {
            LOGGER.info("*** class {} already analyzed, using cached result", klass.getQualifiedName());
            return classToAnalysisResultMap.get(klass);
        }
        else {
            if(!klass.isSubtypeOf(MatcherHelper.getTypeReference(FlowLogic.class))) {
                throw new IllegalArgumentException("Class " +klass.getQualifiedName() +" doesn't extend FlowLogic");
            }
            LOGGER.info("*** analyzing class {}", klass.getQualifiedName());
            final CtMethod callMethod = StaticAnalyzerUtils.findCallMethod(klass);
            if (callMethod == null) {
                throw new AnalysisErrorException(klass, "No call method found");
            }
            if (callMethod.isAbstract()) {
                String exMessage = "Found only an abstract call method";
                if (callMethod.getParent() instanceof CtClass) {
                    exMessage += " in class " + ((CtClass) (callMethod).getParent()).getQualifiedName();
                }
                throw new AnalysisErrorException(klass, exMessage);
            }

            setCurrentAnalyzingClass(klass);

            AnalysisResult res = new AnalysisResult(ClassDescriptionContainer.fromClass(klass));
            res.getClassDescription().setReturnType(StaticAnalyzerUtils.nullifyIfVoidTypeAndGetString(callMethod.getType()));

            final Branch interestingStatements = MatcherHelper.fromCtStatementsToStatements(
                    callMethod.getBody().getStatements(), this);
            res.setStatements(interestingStatements);

            //is it only a "container" flow with no initiating call or also calls initiateFlow(...)?
            final boolean isInitiatingFlow =
                    interestingStatements.getInitiateFlowStatementAtThisLevel().isPresent();

            LOGGER.debug("Contains initiate call? {}", isInitiatingFlow);
            if (isInitiatingFlow) {
                CtClass initiatedFlowClass = getDeeperClassInitiatedBy(klass);

                if (initiatedFlowClass != null) {
                    res.setCounterpartyClassResult(analyzeFlowLogicClass(initiatedFlowClass));
                }
                else {
                    LOGGER.error("Class {} contains initiateFlow call, but can't find corresponding class", klass.getQualifiedName());
                }
                if(drawArrows) {
                    final boolean validProtocol =
                            res.checkIfContainsValidProtocolAndSetupLinks();//check the protocol and draws possible links
                    LOGGER.info("Class {} contains valid protocol? {}", klass.getQualifiedName(), validProtocol);
                }
                else {
                    LOGGER.info("Set on not drawing arrows, the protocol is not figured out");
                }
            }

            classToAnalysisResultMap.put(klass, res);
            return res;
        }
    }

    public void setCurrentAnalyzingClass(CtClass<?> klass) {
        currClassCallStackHolder = ClassCallStackHolder.fromCtClass(klass);
    }

    public ClassCallStackHolder getCurrClassCallStackHolder() {
        return currClassCallStackHolder;
    }

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
                final CtExpression referenceToClass =
                        initiatedByAnnotationOptional.get().getAllValues().get("value");

                if(((CtFieldReadImpl) referenceToClass).getVariable().getDeclaringType() == null){
                    LOGGER.warn("Couldn't retrieve declaration of class declared in the @InitiatedBy " +
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
