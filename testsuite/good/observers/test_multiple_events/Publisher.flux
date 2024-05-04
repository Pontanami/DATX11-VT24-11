type Publisher publishes EventSub1, EventSub2 {
   void fireEvent1();
   void fireEvent2();
}
methods {
   void fireEvent1() {
      publish EventSub1.new();
   }
   void fireEvent2() {
      publish EventSub2.new();
   }
}