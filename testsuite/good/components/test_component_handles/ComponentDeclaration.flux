type ComponentDeclaration {
   void componentMethod();
}
components {
   MyComponent c = MyComponent.new() handles componentMethod();
}
main {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.componentMethod();
}