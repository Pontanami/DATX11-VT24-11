type MyComponent2 {
   void componentMethod2a();
   void componentMethod2b();
}
methods {
   void componentMethod2a() {
      System.out.println("Call forwarded to MyComponent2a.componentMethod2a()");
   }
   void componentMethod2b() {
      System.out.println("Call forwarded to MyComponent2b.componentMethod2b()");
   }
}