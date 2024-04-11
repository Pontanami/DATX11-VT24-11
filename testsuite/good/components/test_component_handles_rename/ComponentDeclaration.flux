type ComponentDeclaration {
   void methodAlias();
}
components {
   MyComponent c = MyComponent.new() handles componentMethod() as methodAlias();
}
main {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.methodAlias();
}