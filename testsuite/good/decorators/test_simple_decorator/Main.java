package default_package;

import runtime.observers.*;
import runtime.decorators.*;

public interface Main {
   public static void main(String[] a) {
      final ObjectToDecorate o = ObjectToDecorate._new ( );
      o._addDecorator(_ClassSimpleDecorator._new());
      o.methodToDecorate ( );
   }
}