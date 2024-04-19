type GPS1 {
   void onTrafficChange(TrafficEvent e);
}
methods {
   void onTrafficChange(TrafficEvent e) {
      System.out.println("GPS1 callback");
   }
}
