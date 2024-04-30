package default_package;

import runtime.observers.*;
import runtime.decorators.*;

public interface Main {
   public void test();
   static Main _new() {
      return _ClassMain._new();
   }
   public default DecoratorTag _addDecorator(_Decorator<Main> decorator) {
      throw new UnsupportedOperationException("This object cannot be decorated");
   }
}