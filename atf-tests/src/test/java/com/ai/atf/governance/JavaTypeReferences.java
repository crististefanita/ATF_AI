package com.ai.atf.governance;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreePath;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/** Parses type positions without resolving dependencies or generating compiler output. */
class JavaTypeReferences extends TreePathScanner<Void, Void> {
    private final Set<String> names = new HashSet<>();
    private final Deque<Scope> scopes = new ArrayDeque<>();

    private JavaTypeReferences() { scopes.push(new Scope()); }

    static Set<String> simpleTypes(String source) {
        JavaTypeReferences references = new JavaTypeReferences();
        for (CompilationUnitTree unit : parse(source)) references.scan(unit, null);
        return references.names;
    }

    /** Recognized JDK execution shapes; this is structural inspection, not control-flow proof. */
    static List<String> executionPolicyEvents(String source) {
        List<String> events = new ArrayList<>();
        for (CompilationUnitTree unit : parse(source)) inspectExecution(unit, events);
        return events;
    }

    /** Decode Java escapes before distinguishing local paths from regular-expression syntax. */
    static List<String> stringLiterals(String source) {
        List<String> values = new ArrayList<>();
        TreeScanner<Void, Void> scanner = new TreeScanner<>() {
            @Override public Void visitLiteral(LiteralTree node, Void unused) {
                if (node.getValue() instanceof String) values.add((String) node.getValue());
                return null;
            }
        };
        for (CompilationUnitTree unit : parse(source)) scanner.scan(unit, null);
        return values;
    }

    private static List<CompilationUnitTree> parse(String source) {
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Architecture governance requires a JDK compiler");
        JavaFileObject input = new SimpleJavaFileObject(URI.create("string:///GovernanceInput.java"), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return source; }
        };
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null)) {
            JavacTask task = (JavacTask) compiler.getTask(null, files, diagnostics,
                    List.of("-proc:none", "--release", "11"), null, List.of(input));
            List<CompilationUnitTree> units = new ArrayList<>();
            for (CompilationUnitTree unit : task.parse()) units.add(unit);
            return units;
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("Cannot parse Java type references", failure);
        }
    }

    private static void inspectExecution(CompilationUnitTree unit, List<String> events) {
        Map<String, String> imports = new HashMap<>();
        Set<String> staticImports = new HashSet<>();
        for (com.sun.source.tree.ImportTree declaration : unit.getImports()) {
            String name = declaration.getQualifiedIdentifier().toString();
            if (declaration.isStatic()) staticImports.add(name);
            else imports.put(name.substring(name.lastIndexOf('.') + 1), name);
        }
        JavaTypeReferences inspection = new JavaTypeReferences() {
            private final Deque<int[]> handlers = new ArrayDeque<>();
            private int deferredDepth;

            @Override public Void visitImport(com.sun.source.tree.ImportTree node, Void unused) { return null; }
            @Override public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
                if (rawPause(node, imports, staticImports, this::typeShadowed, this::declaresMethod, this::valueShadowed)) events.add("PAUSE");
                if (!handlers.isEmpty() && handlers.peek()[0] == deferredDepth
                        && currentInterrupt(node, imports, staticImports, this::typeShadowed, this::declaresMethod, this::valueShadowed)) {
                    handlers.peek()[1] = 1;
                }
                return super.visitMethodInvocation(node, unused);
            }
            @Override public Void visitMemberSelect(MemberSelectTree node, Void unused) {
                if ((node.getIdentifier().contentEquals("out") || node.getIdentifier().contentEquals("err"))
                        && jdkValueType(node.getExpression(), "System", "java.lang.System", imports, this::typeShadowed, this::valueShadowed)) events.add("CONSOLE");
                return super.visitMemberSelect(node, unused);
            }
            @Override public Void visitIdentifier(IdentifierTree node, Void unused) {
                String name = node.getName().toString();
                if (Set.of("out", "err").contains(name) && !valueShadowed(name)
                        && importedStatic(staticImports, "java.lang.System", name)) events.add("CONSOLE");
                return null;
            }
            @Override public Void visitCatch(CatchTree node, Void unused) {
                boolean interrupted = interruptedType(node.getParameter().getType(), imports, this::typeShadowed);
                int[] handler = {deferredDepth, 0};
                handlers.push(handler);
                try { return super.visitCatch(node, unused); }
                finally {
                    handlers.pop();
                    if (interrupted && handler[1] == 0) events.add("INTERRUPT-RESTORE");
                }
            }
            @Override public Void visitClass(ClassTree node, Void unused) {
                deferredDepth++;
                try { return super.visitClass(node, unused); } finally { deferredDepth--; }
            }
            @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void unused) {
                deferredDepth++;
                try { return super.visitLambdaExpression(node, unused); } finally { deferredDepth--; }
            }
        };
        for (Tree declaration : unit.getTypeDecls()) {
            if (declaration instanceof ClassTree) inspection.scopes.peek().types.add(((ClassTree) declaration).getSimpleName().toString());
        }
        inspection.scan(unit, null);
    }

    private static boolean rawPause(MethodInvocationTree call, Map<String, String> imports,
                                    Set<String> staticImports, Predicate<String> types, Predicate<String> methods,
                                    Predicate<String> variables) {
        Tree select = call.getMethodSelect();
        if (select instanceof IdentifierTree) {
            return ((IdentifierTree) select).getName().contentEquals("sleep") && !methods.test("sleep")
                    && importedStatic(staticImports, "java.lang.Thread", "sleep");
        }
        if (!(select instanceof MemberSelectTree)) return false;
        MemberSelectTree method = (MemberSelectTree) select;
        if (!method.getIdentifier().contentEquals("sleep")) return false;
        Tree receiver = unwrap(method.getExpression());
        if (jdkValueType(receiver, "Thread", "java.lang.Thread", imports, types, variables)) return true;
        if (receiver instanceof MemberSelectTree) {
            MemberSelectTree unit = (MemberSelectTree) receiver;
            return jdkValueType(unit.getExpression(), "TimeUnit", "java.util.concurrent.TimeUnit", imports, types, variables);
        }
        if (receiver instanceof IdentifierTree) {
            String unit = ((IdentifierTree) receiver).getName().toString();
            return !variables.test(unit) && importedStatic(staticImports, "java.util.concurrent.TimeUnit", unit);
        }
        return false;
    }

    private static boolean interruptedType(Tree type, Map<String, String> imports, Predicate<String> types) {
        if (type instanceof AnnotatedTypeTree) return interruptedType(((AnnotatedTypeTree) type).getUnderlyingType(), imports, types);
        if (type instanceof UnionTypeTree) return ((UnionTypeTree) type).getTypeAlternatives().stream()
                .anyMatch(alternative -> interruptedType(alternative, imports, types));
        return jdkType(type, "InterruptedException", "java.lang.InterruptedException", imports, types);
    }

    private static boolean currentInterrupt(MethodInvocationTree call, Map<String, String> imports,
                                             Set<String> staticImports, Predicate<String> types,
                                             Predicate<String> methods, Predicate<String> variables) {
        Tree select = call.getMethodSelect();
        if (!call.getArguments().isEmpty() || !(select instanceof MemberSelectTree)) return false;
        MemberSelectTree method = (MemberSelectTree) select;
        Tree receiver = unwrap(method.getExpression());
        return method.getIdentifier().contentEquals("interrupt") && receiver instanceof MethodInvocationTree
                && currentThread((MethodInvocationTree) receiver, imports, staticImports, types, methods, variables);
    }

    private static boolean currentThread(MethodInvocationTree call, Map<String, String> imports,
                                         Set<String> staticImports, Predicate<String> types,
                                         Predicate<String> methods, Predicate<String> variables) {
        if (!call.getArguments().isEmpty()) return false;
        Tree select = call.getMethodSelect();
        if (select instanceof IdentifierTree) return ((IdentifierTree) select).getName().contentEquals("currentThread")
                && !methods.test("currentThread") && importedStatic(staticImports, "java.lang.Thread", "currentThread");
        return select instanceof MemberSelectTree && ((MemberSelectTree) select).getIdentifier().contentEquals("currentThread")
                && jdkValueType(((MemberSelectTree) select).getExpression(), "Thread", "java.lang.Thread", imports, types, variables);
    }

    private static boolean importedStatic(Set<String> imports, String owner, String member) {
        return imports.contains(owner + '.' + member) || imports.contains(owner + ".*");
    }

    private static boolean jdkType(Tree expression, String simple, String qualified,
                                   Map<String, String> imports, Predicate<String> types) {
        String name = unwrap(expression).toString();
        return name.equals(qualified) || name.equals(simple) && !types.test(simple)
                && (!imports.containsKey(simple) || imports.get(simple).equals(qualified));
    }

    private static boolean jdkValueType(Tree expression, String simple, String qualified, Map<String, String> imports,
                                        Predicate<String> types, Predicate<String> variables) {
        return jdkType(expression, simple, qualified, imports, types)
                && (!unwrap(expression).toString().equals(simple) || !variables.test(simple));
    }

    private static Tree unwrap(Tree tree) {
        while (tree instanceof ParenthesizedTree) tree = ((ParenthesizedTree) tree).getExpression();
        return tree;
    }

    private void type(Tree tree) {
        if (tree == null) return;
        new TreeScanner<Void, Void>() {
            @Override public Void visitIdentifier(IdentifierTree identifier, Void unused) {
                String name = identifier.getName().toString();
                if (!typeShadowed(name)) names.add(name);
                return null;
            }
        }.scan(tree, null);
    }

    @Override public Void visitVariable(VariableTree node, Void unused) {
        scopes.peek().variables.add(node.getName().toString());
        type(node.getType());
        return super.visitVariable(node, unused);
    }

    @Override public Void visitMethod(MethodTree node, Void unused) {
        Scope scope = new Scope();
        node.getTypeParameters().forEach(parameter -> scope.types.add(parameter.getName().toString()));
        node.getParameters().forEach(parameter -> scope.variables.add(parameter.getName().toString()));
        scopes.push(scope);
        try {
            type(node.getReturnType());
            node.getThrows().forEach(this::type);
            return super.visitMethod(node, unused);
        } finally { scopes.pop(); }
    }

    @Override public Void visitClass(ClassTree node, Void unused) {
        String name = node.getSimpleName().toString();
        if (getCurrentPath().getParentPath() != null
                && getCurrentPath().getParentPath().getLeaf() instanceof BlockTree) scopes.peek().types.add(name);
        Scope scope = new Scope();
        scope.types.add(name);
        node.getTypeParameters().forEach(parameter -> scope.types.add(parameter.getName().toString()));
        for (Tree member : node.getMembers()) {
            if (member instanceof ClassTree) scope.types.add(((ClassTree) member).getSimpleName().toString());
            if (member instanceof VariableTree) scope.variables.add(((VariableTree) member).getName().toString());
        }
        scopes.push(scope);
        try {
            type(node.getExtendsClause());
            node.getImplementsClause().forEach(this::type);
            return super.visitClass(node, unused);
        } finally { scopes.pop(); }
    }

    @Override public Void visitNewClass(NewClassTree node, Void unused) {
        type(node.getIdentifier());
        node.getTypeArguments().forEach(this::type);
        return super.visitNewClass(node, unused);
    }

    @Override public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
        node.getTypeArguments().forEach(this::type);
        return super.visitMethodInvocation(node, unused);
    }

    @Override public Void visitNewArray(NewArrayTree node, Void unused) {
        type(node.getType());
        return super.visitNewArray(node, unused);
    }

    @Override public Void visitTypeCast(TypeCastTree node, Void unused) {
        type(node.getType());
        return super.visitTypeCast(node, unused);
    }

    @Override public Void visitInstanceOf(InstanceOfTree node, Void unused) {
        type(node.getType());
        return super.visitInstanceOf(node, unused);
    }

    @Override public Void visitAnnotation(AnnotationTree node, Void unused) {
        type(node.getAnnotationType());
        return super.visitAnnotation(node, unused);
    }

    @Override public Void visitTypeParameter(TypeParameterTree node, Void unused) {
        node.getBounds().forEach(this::type);
        return super.visitTypeParameter(node, unused);
    }

    @Override public Void visitMemberSelect(MemberSelectTree node, Void unused) {
        if (node.getIdentifier().contentEquals("class")) type(node.getExpression());
        else expressionType(node.getExpression());
        return super.visitMemberSelect(node, unused);
    }

    @Override public Void visitMemberReference(MemberReferenceTree node, Void unused) {
        expressionType(node.getQualifierExpression());
        if (node.getTypeArguments() != null) node.getTypeArguments().forEach(this::type);
        return super.visitMemberReference(node, unused);
    }

    @Override public Void visitBlock(BlockTree node, Void unused) {
        scopes.push(new Scope());
        try { return super.visitBlock(node, unused); } finally { scopes.pop(); }
    }

    @Override public Void visitForLoop(ForLoopTree node, Void unused) {
        scopes.push(new Scope());
        try { return super.visitForLoop(node, unused); } finally { scopes.pop(); }
    }

    @Override public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void unused) {
        scan(node.getExpression(), unused);
        Scope scope = new Scope();
        scope.variables.add(node.getVariable().getName().toString());
        scopes.push(scope);
        try {
            scan(node.getVariable(), unused);
            scan(node.getStatement(), unused);
        } finally { scopes.pop(); }
        return null;
    }

    @Override public Void visitCatch(CatchTree node, Void unused) {
        scopes.push(new Scope());
        try { return super.visitCatch(node, unused); } finally { scopes.pop(); }
    }

    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void unused) {
        scopes.push(new Scope());
        try { return super.visitLambdaExpression(node, unused); } finally { scopes.pop(); }
    }

    @Override public Void visitTry(TryTree node, Void unused) {
        scopes.push(new Scope());
        try {
            scan(node.getResources(), unused);
            scan(node.getBlock(), unused);
        } finally { scopes.pop(); }
        scan(node.getCatches(), unused);
        scan(node.getFinallyBlock(), unused);
        return null;
    }

    private void expressionType(Tree expression) {
        if (expression instanceof IdentifierTree) {
            String name = ((IdentifierTree) expression).getName().toString();
            if (!typeShadowed(name) && scopes.stream().noneMatch(scope -> scope.variables.contains(name))) names.add(name);
        } else if (expression.getKind() == Tree.Kind.ARRAY_TYPE || expression.getKind() == Tree.Kind.PARAMETERIZED_TYPE
                || expression.getKind() == Tree.Kind.ANNOTATED_TYPE) type(expression);
    }

    final boolean typeShadowed(String name) {
        return scopes.stream().anyMatch(scope -> scope.types.contains(name));
    }

    final boolean valueShadowed(String name) {
        return scopes.stream().anyMatch(scope -> scope.variables.contains(name));
    }

    final boolean declaresMethod(String name) {
        for (TreePath path = getCurrentPath(); path != null; path = path.getParentPath()) {
            if (!(path.getLeaf() instanceof ClassTree)) continue;
            for (Tree member : ((ClassTree) path.getLeaf()).getMembers()) {
                if (member instanceof MethodTree && ((MethodTree) member).getName().contentEquals(name)) return true;
            }
        }
        return false;
    }

    private static final class Scope {
        final Set<String> types = new HashSet<>();
        final Set<String> variables = new HashSet<>();
    }
}
