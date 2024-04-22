type ComponentDeclaration {
   void componentMethod1a();
   void componentMethod1b();
   void renamedMethod2a();
   void renamedMethod2b();
}
components {
   MyComponent c1 = MyComponent1.new() handles componentMethod1a(), componentMethod1b();
   MyComponent c2 = MyComponent2.new() handles
      componentMethod2a() as renamedMethod2a,
      componentMethod2b() as renamedMethod2b;
}
main (String[] args) {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.componentMethod1a();
   cd.componentMethod1b();
   cd.renamedMethod2a();
   cd.renamedMethod2b();
}