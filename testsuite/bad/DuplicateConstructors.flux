// Two constructors cannot have the same signature

type DuplicateConstructors {

}
attributes {
   int i;
}
constructors {
   bad() {
      this.i = 1;
   }
   bad() {
      this.i = 2;
   }
}