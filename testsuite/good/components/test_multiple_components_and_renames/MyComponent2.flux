type MyComponent {
   void componentMethod2a();
   void componentMethod2b();
}
methods {
   void componentMethod2a() {
      System.out.println("Call forwarded to MyComponent.componentMethod2a()");
   }
   void componentMethod2b() {
      System.out.println("Call forwarded to MyComponent.componentMethod2b()");
   }
}