type TrafficController_A publishes TrafficEvent {
   void fireEvent();
}
methods {
   void fireEvent() {
      publish TrafficEvent.new();
   }
}