type Main {}
main (String[] a) {
   PublisherType p = PublisherType.new();
   Subscriber s = Subscriber.new();
   p add subscriber s::print (int);

   p.fireInt(0); // should print 0
   p.fireString("hello"); //should have no effect, no subscribers for strings

   DecoratorRef ref = p add decorator Decorator.new(1);
   p add decorator Decorator.new(2);
   p add subscriber s::print(String, int);

   p.fireInt(p.getInt()); // should print 3
   p.fireString("hello"); // should print Decorator-Decorator-hello

   p remove subscriber s::print (int);
   p remove decorator ref;

   p.fireInt(0); // should have no effect, no subscriber for int
   p.fireString("world"); // should print Decorator-world
}