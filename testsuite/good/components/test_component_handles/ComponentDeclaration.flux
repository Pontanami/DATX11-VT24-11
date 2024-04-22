type ComponentDeclaration {
   void componentMethod();
}
components {
   MyComponent c = MyComponent.new() handles componentMethod();
}
main (String[] args) {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.componentMethod();
}