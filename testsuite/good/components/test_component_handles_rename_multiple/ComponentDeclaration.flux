type ComponentDeclaration {
   void alias1();
   void alias2();
}
components {
   MyComponent c = MyComponent.new() handles componentMethod1() as alias1, componentMethod2() as alias2;
}
main (String[] args){
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.alias1();
   cd.alias2();
}