type PublisherSubType extends PublisherSuperType publishes Event3 {
   void test1();
   void test2();
   void test3();
}
methods {
   void test1() {
      publish Event1.new();
   }
   void test2() {
      publish Event2.new();
   }
   void test3() {
      publish Event3.new();
   }
}