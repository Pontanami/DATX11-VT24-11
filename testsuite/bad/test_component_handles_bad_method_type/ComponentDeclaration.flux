// programs with a missmatch in method signatures for 'handles' clause should be rejected

type ComponentDeclaration {
   void componentMethod(int i);
}
components {
   MyComponent c = MyComponent.new() handles componentMethod();
}
main {
   ComponentDeclaration cd = ComponentDeclaration.new();
   cd.componentMethod();
}