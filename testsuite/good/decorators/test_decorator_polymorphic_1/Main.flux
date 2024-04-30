type Main {}
main (String[] a) {
   SuperType s = SubType.new();
   s add decorator SuperTypeDecorator.new();
   System.out.println(s.methodToDecorate());
}