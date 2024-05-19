type Printer {
   void printIntArray(int[] arr);
   void printDoubleArray(double[] arr);
   void printString2DArray(String[][] arr);
}
methods {
   void printIntArray(int[] arr) {
      System.out.println(arr[0]);
   }
   void printDoubleArray(double[] arr) {
      System.out.println(arr[0]);
   }
   void printString2DArray(String[][] arr) {
      System.out.println(arr[0][0]);
   }
}
main (String[] a) {
   Publisher p = Publisher.new();
   Printer pr = Printer.new();
   p add subscriber pr::printIntArray;
   p add subscriber pr::printDoubleArray;
   p add subscriber pr::printString2DArray;

   p.fireIntArray(int[].of(1));
   p.fireDoubleArray(double[].of(1.0));
   p.fireString2DArray(String[][].of(String[].of("hello")));
}