type PublisherBadEventType publishes Event1 {
   void test();
}
methods {
   void test() {
      publish Event2.new(); // should be caught at compile time
   }
}