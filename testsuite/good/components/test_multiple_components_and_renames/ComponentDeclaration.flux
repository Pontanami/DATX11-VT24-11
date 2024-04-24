type ComponentDeclaration {
   void componentMethod1a();
   void componentMethod1b();
   void renamedMethod1();
   void renamedMethod2();
}
components {
   MyComponent c1 = MyComponent1.new() handles componentMethod1a(), componentMethod1b();
   MyComponent c2 = MyComponent2.new() handles
      componentMethod2a() as renamedMethod1,
      componentMethod2b() as renamedMethod2
}
main (String[] args){
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.componentMethod1();
   cd.componentMethod2();
   cd.renamedMethod1();
   cd.renamedMethod2();
}