// Check that a publisher extending another publisher can publish all the events that the supertype can
// and that the subtype can publish additional events that the supertype can't
type Main {}
main (String[] args) {
   Subscriber s = Subscriber.new();
   Publisher p = PublisherSubType.new();
   
   p add subscriber s::onEvent1;
   p add subscriber s::onEvent2;
   p add subscriber s::onEvent3;
   
   p.test1();
   p.test2();
   p.test3();
}