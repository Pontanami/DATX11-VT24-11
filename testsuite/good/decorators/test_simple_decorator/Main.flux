type Main {}
main (String[] a) {
   ObjectToDecorate o = ObjectToDecorate.new();
   o add decorator SimpleDecorator.new();
   o.methodToDecorate();
}