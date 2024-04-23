type Subscriber {
   void onEvent1(Event1 e);
   void onEvent2(Event2 e);
   void onEvent3(Event3 e);
}
methods {
   void onEvent1(Event1 e) {
      System.out.println("callback1");
   }
   void onEvent2(Event2 e) {
      System.out.println("callback2");
   }
   void onEvent3(Event3 e) {
      System.out.println("callback3");
   }
}