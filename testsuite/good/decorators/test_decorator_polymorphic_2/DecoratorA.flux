decorator DecoratorA decorates A
methods {
   void methodA() {
      System.out.println("DecoratorA.methodA");
      base.methodA();
   }
}