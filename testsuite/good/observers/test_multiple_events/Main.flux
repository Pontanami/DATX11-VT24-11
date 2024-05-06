type Main {}
main (String[] a) {
   Publisher p1 = Publisher.new();
   Subscriber s1 = Subscriber.new();

   p1 add subscriber s1::onEvent (EventSub1, EventSub2);
   p1.fireEvent1();
   p1.fireEvent2();

   Publisher p2 = Publisher.new();
   Subscriber s2 = Subscriber.new();

   p2 add subscriber s2::onEvent (EventSub1, EventSub2);
   p2.fireEvent1();
   p2.fireEvent2();
}