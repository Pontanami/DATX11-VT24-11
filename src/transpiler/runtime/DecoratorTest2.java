package transpiler.runtime;

public class DecoratorTest2 {
    /*
    type Car {
        void gas();
        void brake(float f);
    }
     */

    interface Car {
        void gas();

        void brake(float f);

        default DecoratorTag addDecorator(Decorator<Car> decorator) {
            throw new UnsupportedOperationException();
        }

        static Car _new() {
            return new CarAdapter(new ClassCar());
        }
    }

    static class ClassCar implements Car {
        @Override
        public void gas() {
            System.out.println("basic car gas");
        }
        @Override
        public void brake(float f) {
            System.out.println("basic car brake");
        }
    }

    interface SportsCar extends Car {
        void turbo();
    }

    // decorator Muffler1 decorates Car
    static class Muffler1 extends AbstractDecorator<Car> implements Car {
        @Override
        public Car getDecoratedInstance() {
            return this;
        }

        @Override
        public void gas() {
            getBase().gas();
        }

        @Override
        public void brake(float f) {
            getBase().brake(f);
            System.out.println("Muffler1.brake()");
        }
    }

    static class Muffler2 extends AbstractDecorator<Car> implements Car {
        @Override
        public Car getDecoratedInstance() {
            return this;
        }

        @Override
        public void gas() {
            getBase().gas();
        }

        @Override
        public void brake(float f) {
            getBase().brake(f);
            System.out.println("Muffler2.brake()");
        }
    }

    static final class CarAdapter implements Car {
        private final DecoratorHandler<Car> decoratorHandler;

        CarAdapter(Car base) {
            decoratorHandler = new DecoratorHandler<>(base);
        }

        @Override
        public void gas() {
            decoratorHandler.currentInstance().gas();
        }

        @Override
        public void brake(float f) {
            decoratorHandler.currentInstance().brake(f);
        }

        @Override
        public DecoratorTag addDecorator(Decorator<Car> decorator) {
            return decoratorHandler.addDecorator(decorator);
        }
    }

    public static void main(String[] args) {
        Car car = Car._new();

        System.out.println();
        System.out.println("------- basic car -----------");
        car.gas();
        car.brake(0);

        System.out.println();
        System.out.println("---------- add muffler x2 ------------");
        // DecoratorTag muffler1 = car add decorator Muffler.new()
        DecoratorTag muffler1 = car.addDecorator(new Muffler1());
        DecoratorTag muffler2 = car.addDecorator(new Muffler2());
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
    }
}
