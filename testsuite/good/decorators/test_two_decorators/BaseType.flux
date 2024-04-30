type BaseType {
   void method1(int arg);
   String method2(String arg);
   int method3(int arg);
}
methods {
   void method1(int arg) {
      System.out.println("BaseType.method1(" + arg + ")");
   }
   String method2(String arg) {
      return arg + arg;
   }
   int method3(int arg) {
      return arg + 1;
   }
}