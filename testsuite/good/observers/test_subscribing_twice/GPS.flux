type GPS {
   void onTrafficChange(TrafficEvent e);
}
methods {
   void onTrafficChange(TrafficEvent e) {
      System.out.println("GPS callback");
   }
}
