package java_builder;

public class Examples {
    public static void main(String[] args) {
        System.out.println("-------------------------------------------------------");
        System.out.println(classExample().toCode(new SpaceIndentation(3)));
        System.out.println("-------------------------------------------------------");
        System.out.println(interfaceExample().toCode(new SpaceIndentation(3)));
        System.out.println("-------------------------------------------------------");
    }

    private static ClassBuilder classExample() {
        return new ClassBuilder()
                .addImport("java.util.ArrayList")
                .addImport("java.io.FileReader")
                .addModifier("public")
                .addModifier("final")
                .setIdentifier("MyClass")
                .addField("private int x;")
                .addField("private int y;")
                .addConstructor(new MethodBuilder()
                        .addModifier("public")
                        .setIdentifier("MyClass")
                        .addStatement("this.x = 2;")
                        .addStatement("this.y = 3;")
                ).addMethod(new MethodBuilder()
                        .addModifier("public")
                        .setReturnType("int")
                        .setIdentifier("getX")
                        .addStatement("return x;")
                ).addMethod(new MethodBuilder()
                        .addModifier("public")
                        .addModifier("static")
                        .setReturnType("void")
                        .setIdentifier("main")
                        .addParameter("String[]", "args")
                        .addStatement("System.out.println(\"Hello, world!\")")
                );
    }

    private static InterfaceBuilder interfaceExample() {
        return new InterfaceBuilder()
                .addModifier("public")
                .setIdentifier("MyInterface")
                .addMethod(new MethodBuilder(false)
                        .setReturnType("void")
                        .setIdentifier("interfaceMethod")
                ).addMethod(new MethodBuilder()
                        .addModifier("default")
                        .setReturnType("int")
                        .setIdentifier("twice")
                        .addParameter("int", "x")
                        .addStatement("return x + x;")
                );
    }
}
