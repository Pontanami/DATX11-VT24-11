decorator SimpleDecorator decorates ObjectToDecorate
methods {
   void methodToDecorate() {
      System.out.println("decorator.methodToDecorate");
      base.methodToDecorate();
   }
}