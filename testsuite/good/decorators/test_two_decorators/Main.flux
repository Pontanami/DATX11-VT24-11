// method1 should be handled by Decorator1
// method2 should be handled by BaseType
// method3 should be handled by all objects (incementing the result from base by 1)
type Main {}
main (String[] a) {
   BaseType b = BaseType.new();
   b add decorator Decorator1.new();
   b add decorator Decorator2.new();
   b.method1(0);
   System.out.println(b.method2("a"));
   System.out.println(b.method3(0));
}