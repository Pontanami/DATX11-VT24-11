type MyComponent {
   void componentMethod();
}
attributes {
   int id;
}
constructors {
   new(theId) { id = theId; }
}
methods {
   void componentMethod() {
      System.out.println("Call forwarded to MyComponent#" + id + ".componentMethod()");
   }
}