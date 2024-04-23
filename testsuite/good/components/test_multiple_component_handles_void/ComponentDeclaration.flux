type ComponentDeclaration {
   void componentMethod();
}
components {
   MyComponent c1 = MyComponent.new(1) handles componentMethod();
   MyComponent c2 = MyComponent.new(2) handles componentMethod();
}
main (String[] args) {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.componentMethod();
}