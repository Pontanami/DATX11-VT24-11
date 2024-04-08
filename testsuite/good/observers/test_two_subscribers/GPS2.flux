type GPS2 {
   void onTrafficChange(TrafficEvent e);
}
methods {
   void onTrafficChange(TrafficEvent e) {
      System.out.println("GPS2 callback");
   }
}