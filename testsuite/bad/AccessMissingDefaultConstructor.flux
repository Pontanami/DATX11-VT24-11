// check that we don't generate a default constructor when one has been declared
type AccessMissingDefaultConstructor {}
constructors {
   custom() {}
}
main {
   AccessMissingDefaultConstructor a = AccessMissingDefaultConstructor.new();
}