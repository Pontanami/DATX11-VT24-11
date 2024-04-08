// subscring a second time with the same object and method to the same event should have no effect
type GPS {
   void onTrafficChange(TrafficEvent e);
}
methods {
   void onTrafficChange(TrafficEvent e) {
      System.out.println("GPS callback");
   }
}
main {
   TrafficController tc = TrafficController.new();
   GPS gps = GPS.new();
   tc add subscriber gps.onTrafficChange;
   tc add subscriber gps.onTrafficChange;
   tc.fireEvent();
}