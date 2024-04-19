// programs with a missmatch in method signatures for 'handles as' clause should be rejected

type ComponentDeclaration {
   void renamedMethod(int i);
}
components {
   MyComponent c = MyComponent.new() handles componentMethod as renamedMethod;
}
main {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.renamedMethod();
}