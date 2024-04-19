type TrafficController_B publishes TrafficEvent {
   void fireEvent();
}
methods {
   void fireEvent() {
      publish TrafficEvent.new();
   }
}