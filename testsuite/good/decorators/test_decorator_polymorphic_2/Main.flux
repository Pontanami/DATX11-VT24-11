type Main {}
main (String[] args) {
   C c = C.new();
   B bc = c;
   A ac = c;

   DecoratorTag ta = c add decorator DecoratorA.new();
   DecoratorTag tb = bc add decorator DecoratorB.new();
   DecoratorTag tc = c add decorator DecoratorC.new();

   ac.methodA();
   tb.deleteDecorator();
   System.out.println();

   bc.methodB();
   System.out.println();

   DecoratorTag ta1 = ac add decorator DecoratorA.new();
   bc add decorator DecoratorA.new();

   c.methodC();
   System.out.println();

   ac.methodA();
   System.out.println();

   tc.deleteDecorator();
   ta1.deleteDecorator();
   ta.deleteDecorator();
   c.methodA();
}