type ComponentDeclaration {
   void methodAlias();
}
components {
   MyComponent c = MyComponent.new() handles componentMethod() as methodAlias();
}
main (String[] args) {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.methodAlias();
}