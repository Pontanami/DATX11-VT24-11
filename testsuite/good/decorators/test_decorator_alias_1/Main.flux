type Main {
   void test();
}
methods {
   void test() {
      ObjectToDecorate o = ObjectToDecorate.new();
      helper(o);
      o.methodToDecorate();
   }
   void helper(ObjectToDecorate o) {
      o add decorator SimpleDecorator.new();
   }
}
main (String[] a) {
   Main.new().test();
}