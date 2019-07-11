package com.github.lucacampanella.callgraphflows.staticanalyzer;

import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import spoon.reflect.declaration.CtClass;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassDescriptionContainer {
    private String simpleName;
    private String containingClassName;
    private String packageName;
    private String fullyQualifiedName;
    private String comments;
    private String returnType = null; //the type returned by the call method
    private static final List<Class<? extends Annotation>> importantAnnotations =
            Arrays.asList(InitiatingFlow.class, StartableByRPC.class);
    private Set<String> annotations = new HashSet<>();

    public ClassDescriptionContainer(String simpleName, String containingClassName, String packageName,
                                     String fullyQualifiedName, String comments, Set<String> annotations) {
        this.simpleName = simpleName;
        this.containingClassName = containingClassName;
        this.packageName = packageName;
        this.fullyQualifiedName = fullyQualifiedName;
        this.comments = comments;
        this.annotations = annotations;
    }

    public static ClassDescriptionContainer fromClass(CtClass klass) {
        String simpleName;
        String containingClassName;
        String packageName;
        String fullyQualifiedName;
        String comments;

        simpleName = klass.getSimpleName();
        containingClassName = klass.getTopLevelType().getSimpleName() == simpleName ?
                null : klass.getTopLevelType().getSimpleName();
        packageName = klass.getPackage().getQualifiedName();
        fullyQualifiedName = klass.getQualifiedName();
        comments = klass.getDocComment();

        final Set<String> presentAnnotations = importantAnnotations.stream().filter(annotClass ->
                klass.hasAnnotation(annotClass)).map(annotClass -> annotClass.getSimpleName())
                .collect(Collectors.toSet());

        return new ClassDescriptionContainer(simpleName, containingClassName, packageName, fullyQualifiedName,
                comments, presentAnnotations);
    }

    public static ClassDescriptionContainer getEmpty() {
        return new ClassDescriptionContainer(null, null, null, null, null, null);
    }

    public void addImportantAnnotation(String annotationWithAt) {
        annotations.add(annotationWithAt);
    }

    public Set<String> getAnnotations() {
        return annotations;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public boolean hasContainingClass() {
        return containingClassName != null;
    }

    public String getContainingClassName() {
        return containingClassName;
    }

    public String getContainingClassNameOrItself() {
        return hasContainingClass() ? getContainingClassName() : getSimpleName();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public String getComments() {
        return comments;
    }

    public String getNameWithParent() {
        return hasContainingClass() ? containingClassName + "$" + simpleName : simpleName;
    }

    @Override
    public boolean equals(Object classDescriptionContainer) {
        if(classDescriptionContainer instanceof ClassDescriptionContainer) {
            return ((ClassDescriptionContainer) classDescriptionContainer).getFullyQualifiedName()
                    .equals(getFullyQualifiedName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getFullyQualifiedName().hashCode();
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getReturnType() {
        return returnType;
    }
}
