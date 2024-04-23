type MyComponent1 {
   void componentMethod1a();
   void componentMethod1b();
}
methods {
   void componentMethod1a() {
      System.out.println("Call forwarded to MyComponent1a.componentMethod1a()");
   }
   void componentMethod1b() {
      System.out.println("Call forwarded to MyComponent1b.componentMethod1b()");
   }
}