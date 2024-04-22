// test that removing actually removes and adding actually adds, when using 2 subscribers
type Main {}
main (String[] args) {
   TrafficController tc = TrafficController.new();
   GPS1 gps1 = GPS1.new();
   GPS2 gps2 = GPS2.new();
   tc add subscriber gps1.onTrafficChange;
   tc add subscriber gps2.onTrafficChange;
   tc remove subscriber gps1.onTrafficChange;
   tc.fireEvent();
   tc add subscriber gps1.onTrafficChange;
   tc remove subscriber gps2.onTrafficChange;
   tc.fireEvent();
}