type CallMethodFromDelegate{
    void printSomething();
}
components{
   Component1 c1 = Component1.new() handles printSomething();
}
methods {
   void printSomething() {
      c1.printSomething();
   }
}

main (String[] arguments){
    CallMethodFromDelegate cmfd = CallMethodFromDelegate.new();
    cmfd.printSomething();
}