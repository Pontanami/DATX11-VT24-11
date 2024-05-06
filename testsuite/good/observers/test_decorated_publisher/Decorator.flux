decorator Decorator decorates PublisherType
constructors {
   new(int i) {
      this.i = i;
   }
}
attributes {
   int i;
}
methods {
   int getInt() {
      return base.getInt() + i;
   }
   
   void fireString(String s) {
      base.fireString("Decorator-" + s);
   }
}