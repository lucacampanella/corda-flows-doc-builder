package com.github.lucacampanella.callgraphflows.staticanalyzer;

import net.corda.core.flows.InitiatedBy;
import spoon.Launcher;
import spoon.compiler.SpoonResourceHelper;
import spoon.legacy.NameFilter;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.Factory;
import spoon.reflect.path.CtPath;
import spoon.reflect.path.CtPathStringBuilder;
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtFieldReadImpl;

import java.util.*;

public class SourceClassAnalyzer extends AnalyzerWithModel {

    public SourceClassAnalyzer(String pathToClass) {
        Launcher spoon = new Launcher();
        spoon.addInputResource(pathToClass);
        spoon.buildModel();
        model = spoon.getModel();
    }

    public static Map<CtClass, CtClass> getInitiatedClassToInitiatingMap(CtClass containerClass) {
        //find all classes that are initiated by flows
        List<CtClass> initiatedClasses = containerClass.getElements(new AnnotationFilter<>(InitiatedBy.class));

        //retain only the ones that are furthest away from FlowLogic.class, these are the one that will actually
        // be run by corda
        Set<CtClass> furthestInitiatedClasses = new HashSet<>();
        initiatedClasses.forEach(klass -> furthestInitiatedClasses.add(getFurthestAwaySubclass(klass, containerClass)));

        //map them to the class that initiates them
        Map<CtClass, CtClass> initiatedClassToInitiating = new HashMap<>(initiatedClasses.size());

        for(CtClass klass : furthestInitiatedClasses) {
            CtAnnotation initiatedByAnnotation = klass.getAnnotations().stream().filter(ctAnnotation ->
                    ctAnnotation.getActualAnnotation().annotationType() == InitiatedBy.class).findFirst().get();

            final CtExpression referenceToClass = (CtExpression) initiatedByAnnotation.getAllValues().get("value");

            String stringPath = ((CtFieldReadImpl) referenceToClass).getTarget().toString();
            String className = stringPath.substring(stringPath.lastIndexOf(".")+1);

            final CtClass initiatingClass = (CtClass) containerClass.getElements(new NameFilter<>(
                    className)).get(0);

            initiatedClassToInitiating.put(klass, initiatingClass);

        }
        return initiatedClassToInitiating;
    }

    public static CtClass getFurthestAwaySubclass(CtClass superClass, CtClass modelClass) {
        List<CtClass> allClasses = modelClass.getElements(new TypeFilter<>(CtClass.class));

        CtClass furthestAway = superClass;

        for(CtClass subClass : allClasses) {
            if (subClass.isSubtypeOf(superClass.getReference())
                    && subClass.isSubtypeOf(furthestAway.getReference())) {
                furthestAway = subClass;
            }
        }

        return furthestAway;
    }
}
