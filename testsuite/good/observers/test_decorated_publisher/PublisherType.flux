decorable type PublisherType publishes int, String {
   void fireInt(int i);
   void fireString(String s);
   int getInt();
}
methods {
   void fireInt(int i) {
      publish i;
   }
   void fireString(String s) {
      publish s;
   }
   int getInt() {
      return 0;
   }
}