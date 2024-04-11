type MyComponent {
   void componentMethod1();
   void componentMethod2();
}
methods {
   void componentMethod1() {
      System.out.println("Call forwarded to MyComponent.componentMethod1()");
   }
   void componentMethod2() {
      System.out.println("Call forwarded to MyComponent.componentMethod1()");
   }
}