package com.github.lucacampanella.callgraphflows.staticanalyzer;

import spoon.reflect.declaration.CtClass;

public class ClassDescriptionContainer {
    private String simpleName;
    private String containingClassName;
    private String packageName;
    private String fullyQualifiedName;
    private String comments;

    public ClassDescriptionContainer(String simpleName, String containingClassName, String packageName, String fullyQualifiedName, String comments) {
        this.simpleName = simpleName;
        this.containingClassName = containingClassName;
        this.packageName = packageName;
        this.fullyQualifiedName = fullyQualifiedName;
        this.comments = comments;
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

        return new ClassDescriptionContainer(simpleName, containingClassName, packageName, fullyQualifiedName, comments);
    }

    public static ClassDescriptionContainer getEmpty() {
        return new ClassDescriptionContainer(null, null, null, null, null);
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
}
