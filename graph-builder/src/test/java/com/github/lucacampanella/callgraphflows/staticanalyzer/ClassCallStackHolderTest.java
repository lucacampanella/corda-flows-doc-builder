package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.DoubleExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.ExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.InitiatorBaseFlow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.NamedElementFilter;

import static com.github.lucacampanella.TestUtils.fromClassSrcToPath;
import static org.assertj.core.api.Assertions.assertThat;

class ClassCallStackHolderTest {

    static AnalyzerWithModel analyzer;
    static CtClass<DoubleExtendingSuperclassTestFlow.Initiator> doubleExtClass;
    static CtClass<InitiatorBaseFlow> initiatorBaseClass;

    @BeforeAll
    static void setUp() {
        analyzer = new SourceClassAnalyzer(fromClassSrcToPath(InitiatorBaseFlow.class),
                fromClassSrcToPath(ExtendingSuperclassTestFlow.class),
                fromClassSrcToPath(DoubleExtendingSuperclassTestFlow.class));
        doubleExtClass = analyzer.getClass(DoubleExtendingSuperclassTestFlow.Initiator.class);
        initiatorBaseClass = analyzer.getClass(InitiatorBaseFlow.class);
    }

    @Test
    void fromCtClass() {
        ClassCallStackHolder classCallStackHolder = ClassCallStackHolder.fromCtClass(doubleExtClass);
        assertThat(classCallStackHolder.getClassStack()).hasSize(3);

        classCallStackHolder = ClassCallStackHolder.fromCtClass(initiatorBaseClass);
        assertThat(classCallStackHolder.getClassStack()).hasSize(1);
    }

    @Test
    void fromCtInvocationToDynamicExecutableRef() {

        final ClassCallStackHolder classCallStackHolder = ClassCallStackHolder.fromCtClass(doubleExtClass);
        final CtMethod callMethod = StaticAnalyzerUtils.findCallMethod(initiatorBaseClass);
        final CtInvocation realCallMethodInv = (CtInvocation) callMethod.getBody().getStatements().get(1);//real call method invocation
        final CtMethod discoveredMethod = classCallStackHolder.fromCtAbstractInvocationToDynamicExecutableRef(realCallMethodInv);

        final CtMethod realCallMethod = doubleExtClass.getElements(
                new NamedElementFilter<CtMethod>(CtMethod.class, "realCallMethod")).stream().findAny().get();

        assertThat(discoveredMethod).isEqualTo(realCallMethod);
    }

    @Test
    void resolveEventualGenerics() {
        final ClassCallStackHolder classCallStackHolder = ClassCallStackHolder.fromCtClass(doubleExtClass);
        final CtMethod callMethod = StaticAnalyzerUtils.findCallMethod(initiatorBaseClass);
        final CtLocalVariable localVariable = (CtLocalVariable) callMethod.getBody().getStatements().get(2);
        final CtTypeReference typeReference = classCallStackHolder.resolveEventualGenerics(localVariable.getType());
        System.out.println(typeReference.toString());
    }
}