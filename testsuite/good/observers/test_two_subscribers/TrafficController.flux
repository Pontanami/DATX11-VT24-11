type TrafficController publishes TrafficEvent {
   void fireEvent();
}
methods {
   void fireEvent() {
      publish TrafficEvent.new();
   }
}