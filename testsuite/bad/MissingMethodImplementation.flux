// if a type has an implementation, it must implement all methods in the type block

type MissingMethodImplementation {
   void presentMethod();
   int missingMethod();
}
methods {
   void presentMethod() {}
}