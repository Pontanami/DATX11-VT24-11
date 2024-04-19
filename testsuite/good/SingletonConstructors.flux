type SingletonConstructors {
   int getId();
}
attributes {
   int id;
}
singleton constructors {
   instance1() {
      this.id = 1;
   }
   instance2() {
      this.id = 2;
   }
}
methods {
   int getId() { return id; }
}
main {
   System.out.println(SingletonConstructors.instance1() == SingletonConstructors.instance1()); //should point to the same object
   System.out.println(SingletonConstructors.instance1() != SingletonConstructors.instance2()); //should not point to the same object
   System.out.println(SingletonConstructors.instance2() == SingletonConstructors.instance2()); //should point to the same object

   System.out.println(SingletonConstructors.instance1().getId() == 1);
   System.out.println(SingletonConstructors.instance2().getId() == 2);
}