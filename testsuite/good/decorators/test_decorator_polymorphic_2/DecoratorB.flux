decorator DecoratorB decorates B
methods {
   void methodA() {
      System.out.println("DecoratorB.methodA");
      base.methodA();
   }
   void methodB() {
      System.out.println("DecoratorB.methodB");
      base.methodB();
   }
}