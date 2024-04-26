type SingletonConstructors {
   int getId();
}
singleton constructors {
   instance1() {
      this.id = 1;
   }
   instance2() {
      this.id = 2;
   }
}
attributes {
   int id;
}
methods {
   int getId() { return id; }
}
main (String[] args) {
   System.out.println(SingletonConstructors.instance1() == SingletonConstructors.instance1()); //should point to the same object
   System.out.println(SingletonConstructors.instance1() != SingletonConstructors.instance2()); //should not point to the same object
   System.out.println(SingletonConstructors.instance2() == SingletonConstructors.instance2()); //should point to the same object

   System.out.println(SingletonConstructors.instance1().getId() == 1);
   System.out.println(SingletonConstructors.instance2().getId() == 2);
}