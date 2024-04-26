type ComponentDeclaration {
   void callsTwoComponents();
}
components {
   MyComponent c1 = MyComponent.new(1) handles componentMethod() as callsTwoComponents;
   MyComponent c2 = MyComponent.new(2) handles componentMethod() as callsTwoComponents;
}
main (String[] args) {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.callsTwoComponents();
}