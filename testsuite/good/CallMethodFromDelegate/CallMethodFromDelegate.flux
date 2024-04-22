type CallMethodFromDelegate{
    void printSomething();
}
components{
   Component1 c1 = Component1.new() handles printSomething();
}

main (String[] arguments){
    CallMethodFromDelegate cmfd = CallMethodFromDelegate.new();
    cmfd.printSomething();
}