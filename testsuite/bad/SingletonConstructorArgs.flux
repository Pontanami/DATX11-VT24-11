// singleton constructors are not allowed to take arguments

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