type SingletonConstructorArgs {

}
attributes {
   int i;
}
singleton constructors {
   bad(int i) {
      this.i = i;
   }
}