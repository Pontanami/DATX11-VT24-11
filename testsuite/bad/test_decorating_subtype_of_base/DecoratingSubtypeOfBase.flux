type DecoratingSubtypeOfBase {
   void bad();
}
methods {
   void bad() {
      A a = A.new();
      a add decorator DecoratorB.new();
   }
}