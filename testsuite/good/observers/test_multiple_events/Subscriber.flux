type Subscriber {
   void onEvent(EventSuper e);
}
methods {
   void onEvent(EventSuper e) {
      System.out.println(e.getInt());
   }
}