// Testar om en subscriber kan subscribea på flera publishers
type GPS {
   void onTrafficChange_A(TrafficEvent e);
   void onTrafficChange_B(TrafficEvent e);
}
methods {
   void onTrafficChange_A(TrafficEvent e) {
      System.out.println("GPS callback A");
   }
   void onTrafficChange_B(TrafficEvent e) {
      System.out.println("GPS callback B");
   }
}
main {
   TrafficController_A tcA = TrafficController_A.new();
   TrafficController_B tcB = TrafficController_B.new();
   GPS gps = GPS.new();
   tcA add subscriber gps.onTrafficChange_A;
   tcB add subscriber gps.onTrafficChange_B;
   tcA.fireEvent();
   tcB.fireEvent();
}