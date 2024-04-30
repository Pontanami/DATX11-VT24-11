decorator Decorator1 decorates BaseType
methods {
   void method1(int arg) {
      System.out.println("Decorator1.method1(" + arg + ")");
   }
   int method3(int arg) {
      return base.method3(arg) + 1;
   }
}