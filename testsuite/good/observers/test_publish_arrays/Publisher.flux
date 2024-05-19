type Publisher publishes int[], double[], String[][] {
   void fireIntArray(int[] arr);
   void fireDoubleArray(double[] arr);
   void fireString2DArray(String[][] arr);
}
methods {
   void fireIntArray(int[] arr) {
      publish arr;
   }
   void fireDoubleArray(double[] arr) {
      publish arr;
   }
   void fireString2DArray(String[][] arr) {
      publish arr;
   }
}