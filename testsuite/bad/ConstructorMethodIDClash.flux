// Methods and constructors cannot have the same signature

type ConstructorMethodIDClash {
   int bad(float badArg);
}
attributes {
   float f;
}
constructors {
   bad(float badArg) {
      this.f = badArg;
   }
}
