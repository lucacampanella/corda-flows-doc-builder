package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;

public class ClassCallStackHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassCallStackHolder.class);

    private List<CtClass<?>> classStack = new ArrayList<>(1);

    public static ClassCallStackHolder fromCtClass(CtClass klass) {
        ClassCallStackHolder classCallStackHolder = new ClassCallStackHolder();

        classCallStackHolder.classStack.add(klass);
        CtTypeReference<?> superclassRef = klass.getSuperclass();
        while(superclassRef != null && !superclassRef.getQualifiedName().startsWith("net.corda")) {
            CtClass curr = (CtClass) superclassRef.getTypeDeclaration();
            if(curr == null) {
                break;
            }
            classCallStackHolder.classStack.add(curr);

            superclassRef = curr.getSuperclass();
        }

        return classCallStackHolder;
    }

    public CtMethod fromCtAbstractInvocationToDynamicExecutableRef(CtInvocation inv) {
        CtMethod correspondingMethod = (CtMethod) inv.getExecutable().getDeclaration();
        if(classStack.size() == 1 || !(inv.getTarget() instanceof CtThisAccess))
        //the class directly extends FlowLogic or the target is some other class and not the one in which is invoked
        {
            return correspondingMethod;
        }

        CtClass callerClass = StaticAnalyzerUtils.getLowerContainingClass(inv);

        for(CtClass<?> curr : classStack) {
            if (curr == callerClass)
            //we arrived at the class calling the method without finding an implementation lower in the class stack
            //this means the method is implemented here for the first time
            {
                return correspondingMethod;
            }
            final Set<CtMethod<?>> currMethods = curr.getMethods();
            for (CtMethod<?> method : currMethods) {
                if (method.isOverriding(correspondingMethod)) {
                    return method; //as soon as we find an overriding method in the call stack we return it
                }
            }
        }

        LOGGER.warn("Couldn't find corresponding method in the call stack for method {} even if should have been found, " +
                        "going on with the default executable, this can cause wrongly drawn graphs",
                correspondingMethod.getSimpleName());

        return correspondingMethod;
    }

    public List<CtClass<?>> getClassStack() {
        return classStack;
    }
}
