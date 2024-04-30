decorator DecoratorC decorates C
methods {
   void methodA() {
      System.out.println("DecoratorC.methodA");
      base.methodA();
   }
   void methodB() {
      System.out.println("DecoratorC.methodB");
      base.methodB();
   }
   void methodC() {
      System.out.println("DecoratorC.methodC");
      base.methodC();
   }
}