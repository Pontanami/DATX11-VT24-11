type MyComponent {
   void componentMethod();
}
constructors {
   Comp(int theId) { id = theId; }
}
attributes {
   int id;
}
methods {
   void componentMethod() {
      System.out.println("Call forwarded to MyComponent#" + id + ".componentMethod()");
   }
}