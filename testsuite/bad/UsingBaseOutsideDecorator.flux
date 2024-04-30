type UsingBaseOutsideDecorator {
   void bad1();
   void bad2();
}
methods {
   void bad1() {}
   void bad2() {
      int x = 2;
      base.bad1();
   }
}