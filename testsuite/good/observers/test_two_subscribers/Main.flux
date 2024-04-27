// test that removing actually removes and adding actually adds, when using 2 subscribers
type Main {}
main (String[] args) {
   TrafficController tc = TrafficController.new();
   GPS1 gps1 = GPS1.new();
   GPS2 gps2 = GPS2.new();
   SubscriberTag tag1 = tc add subscriber gps1::onTrafficChange;
   SubscriberTag tag2 = tc add subscriber gps2::onTrafficChange;
   tag1.unsubscribe();
   tc.fireEvent();
   tc add subscriber gps1::onTrafficChange;
   tag2.unsubscribe();
   tc.fireEvent();
}