type B extends A {
   void methodB();
}
methods {
   void methodA() {
      System.out.println("B.methodA");
   }
   void methodB() {
      System.out.println("B.methodB");
   }
}