decorator SuperTypeDecorator decorates SuperType
methods {
   int methodToDecorate() {
      return base.methodToDecorate() + 1;
   }
}