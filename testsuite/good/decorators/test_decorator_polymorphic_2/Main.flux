type Main {}
main (String[] args) {
   C c = C.new();
   B bc = c;
   A ac = c;

   DecoratorRef ta = c add decorator DecoratorA.new();
   DecoratorRef tb = bc add decorator DecoratorB.new();
   DecoratorRef tc = c add decorator DecoratorC.new();

   ac.methodA();
   bc remove decorator tb;
   System.out.println();

   bc.methodB();
   System.out.println();

   DecoratorRef ta1 = ac add decorator DecoratorA.new();
   bc add decorator DecoratorA.new();

   c.methodC();
   System.out.println();

   ac.methodA();
   System.out.println();

   c remove decorator tc;
   bc remove decorator ta1;
   ac remove decorator ta;
   c.methodA();
}
