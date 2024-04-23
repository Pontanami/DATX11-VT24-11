type ComponentDeclaration {
   void componentMethod1();
   void componentMethod2();
}
components {
   MyComponent c = MyComponent.new() handles componentMethod1(), componentMethod2();
}
main (String[] args) {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.componentMethod1();
   cd.componentMethod2();
}