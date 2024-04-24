package runtime.decorators;

public class DecoratorTest2 {
    /*
    type Car {
        void gas();
        void brake(float f);
    }
     */

    public interface Car {
        void gas();

        void brake(float f);

        default DecoratorTag _addDecorator(_Decorator<Car> decorator) {
            throw new UnsupportedOperationException();
        }

        static Car _new() {
            return new _DecoratedCar(_ClassCar._new());
        }
    }

    static class _ClassCar implements Car {
        @Override
        public void gas() {
            System.out.println("basic car gas");
        }
        @Override
        public void brake(float f) {
            System.out.println("basic car brake");
        }
        static _ClassCar _new() {
            return new _ClassCar();
        }
    }

    interface SportsCar extends Car {
        void turbo();
    }

    public static class _ClassCarLogger extends _AbstractDecorator<Car> implements Car {
        private enum _ConstructorId_new { __ }
        private int callCounter;
        private final StringBuilder log;

        private _ClassCarLogger(_ConstructorId_new __, StringBuilder theLog) {
            callCounter = 0;
            log = theLog;
        }

        public static _ClassCarLogger _new(StringBuilder theLog) {
            return new _ClassCarLogger(_ConstructorId_new.__, theLog);
        }

        public Car _getDecoratedInstance() {
            return this;
        }
        public void gas() {
            logCall("gas()");
            _getBase().gas();
        }
        public void brake(float f) {
            logCall("brake()");
            _getBase().brake(f);
        }
        private void logCall(String methodSig) {
            callCounter++;
            log.append("Method call #").append(callCounter).append(": Car.").append(methodSig).append("\n");
        }
    }

    // decorator Muffler1 decorates Car
    static class _Muffler1 extends _AbstractDecorator<Car> implements Car {
        @Override
        public Car _getDecoratedInstance() {
            return this;
        }

        @Override
        public void gas() {
            _getBase().gas();
        }

        @Override
        public void brake(float f) {
            _getBase().brake(f);
            System.out.println("Muffler1.brake()");
        }
    }

    static class _Muffler2 extends _AbstractDecorator<Car> implements Car {
        @Override
        public Car _getDecoratedInstance() {
            return this;
        }

        @Override
        public void gas() {
            _getBase().gas();
        }

        @Override
        public void brake(float f) {
            _getBase().brake(f);
            System.out.println("Muffler2.brake()");
        }
    }

    static final class _DecoratedCar implements Car {
        private final _DecoratorHandler<Car> decoratorHandler;

        _DecoratedCar(Car base) {
            decoratorHandler = new _DecoratorHandler<>(base);
        }

        @Override
        public void gas() {
            decoratorHandler.getTopLevelDecorator().gas();
        }

        @Override
        public void brake(float f) {
            decoratorHandler.getTopLevelDecorator().brake(f);
        }

        @Override
        public DecoratorTag _addDecorator(_Decorator<Car> decorator) {
            return decoratorHandler.addDecorator(decorator);
        }
    }

    public static void main(String[] args) {
        StringBuilder log = new StringBuilder();
        Car car = Car._new();
        car._addDecorator(_ClassCarLogger._new(log));

        System.out.println();
        System.out.println("------- basic car -----------");
        car.gas();
        car.brake(0);

        System.out.println();
        System.out.println("---------- add muffler x2 ------------");
        // DecoratorTag muffler1 = car add decorator Muffler.new()
        DecoratorTag muffler1 = car._addDecorator(new _Muffler1());
        DecoratorTag muffler2 = car._addDecorator(new _Muffler2());
        car.gas();
        car.brake(0);

        System.out.println();
        System.out.println("------- delete muffler 1 -----------");
        muffler1.delete();
        car.gas();
        car.brake(0);

        System.out.println();
        System.out.println("------- delete muffler 2 -----------");
        muffler2.delete();
        car.gas();
        car.brake(0);

        System.out.println();
        System.out.println("--------------- log ----------------");
        System.out.println(log);
    }
}
