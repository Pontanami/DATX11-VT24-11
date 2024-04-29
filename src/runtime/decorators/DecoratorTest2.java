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

        boolean brake(float f);

        default DecoratorTag _addDecorator(CarDecorator decorator) {
            throw new UnsupportedOperationException();
        }

        static Car _new() {
            return _ClassCar._new();
        }
    }

    static class _ClassCar implements Car {
        @Override
        public void gas() {
            System.out.println("basic car gas");
        }
        @Override
        public boolean brake(float f) {
            System.out.println("basic car brake");
            return true;
        }
        static Car _new() {
            return new _DecoratedCar(new _ClassCar());
        }
    }

    interface SportsCar extends Car {
        void turbo();

        static SportsCar _new() {
            return _ClassSportsCar._new();
        }

        default DecoratorTag _addDecorator(CarDecorator decorator) {
            throw new UnsupportedOperationException();
        }

        default DecoratorTag _addDecorator(SportsCarDecorator decorator) {
            throw new UnsupportedOperationException();
        }
    }

    static class _ClassSportsCar implements SportsCar {
        @Override
        public void gas() {
            System.out.println("basic sports car gas");
        }
        @Override
        public boolean brake(float f) {
            System.out.println("basic sports car brake");
            return true;
        }
        static SportsCar _new() {
            return new _DecoratedSportsCar(new _ClassSportsCar());
        }
        @Override
        public void turbo() {
            System.out.println("basic sports car turbo");
        }
    }

    static abstract class CarDecorator extends _AbstractDecorator<Car> implements Car {
        @Override
        public void gas() {
            _getPrevious()._invoke(void.class, "gas");
        }

        @Override
        public boolean brake(float f) {
            return _getPrevious()._invoke(Boolean.class, "brake", new Class[]{float.class}, new Object[]{f});
        }
    }

    static abstract class SportsCarDecorator extends _AbstractDecorator<SportsCar> implements SportsCar {
        @Override
        public void gas() {
            _getPrevious()._invoke(void.class, "gas");
        }

        @Override
        public boolean brake(float f) {
            return _getPrevious()._invoke(Boolean.class, "brake", new Class[]{float.class}, new Object[]{f});
        }

        @Override
        public void turbo() {
            _getPrevious()._invoke(void.class, "turbo");
        }
    }

    // decorator Muffler1 decorates Car
    static class _Muffler1 extends CarDecorator {
        @Override
        public boolean brake(float f) {
            boolean brakeResult = super.brake(f);
            System.out.println("Muffler1.brake custom behavior");
            return brakeResult;
        }
    }

    static class _Muffler2 extends CarDecorator {
        @Override
        public boolean brake(float f) {
            boolean brakeResult = super.brake(f);
            System.out.println("Muffler2.brake custom behavior");
            return brakeResult;
        }
    }

    static class SportsCarMuffler extends SportsCarDecorator {
        @Override
        public void turbo() {
            super.turbo();
            System.out.println("SportsCarMuffler.turbo custom behavior");
        }
    }

    static final class _DecoratedCar implements Car {
        private final _DecoratorHandler<Car> decoratorHandler;

        _DecoratedCar(Car base) {
            decoratorHandler = new _DecoratorHandler<>(base);
        }

        @Override
        public void gas() {
            decoratorHandler.callTopDecorator(void.class, "gas", new Class[]{}, new Object[]{});
        }

        @Override
        public boolean brake(float f) {
            return decoratorHandler.callTopDecorator(Boolean.class, "brake", new Class[]{float.class}, new Object[]{f});
        }

        @Override
        public DecoratorTag _addDecorator(CarDecorator decorator) {
            return decoratorHandler.addDecorator(decorator);
        }
    }

    static final class _DecoratedSportsCar implements SportsCar {
        private final _DecoratorHandler<SportsCar> decoratorHandler;

        _DecoratedSportsCar(SportsCar base) {
            decoratorHandler = new _DecoratorHandler<>(base);
        }

        @Override
        public void gas() {
            decoratorHandler.callTopDecorator(void.class, "gas", new Class[]{}, new Object[]{});
        }

        @Override
        public boolean brake(float f) {
            return decoratorHandler.callTopDecorator(Boolean.class, "brake", new Class[]{float.class}, new Object[]{f});
        }

        @Override
        public void turbo() {
            decoratorHandler.callTopDecorator(void.class, "turbo", new Class[]{}, new Object[]{});
        }

        @Override
        public DecoratorTag _addDecorator(CarDecorator decorator) {
            return decoratorHandler.addDecorator(decorator);
        }

        @Override
        public DecoratorTag _addDecorator(SportsCarDecorator decorator) {
            return decoratorHandler.addDecorator(decorator);
        }
    }

    interface CanBeDecorated {}


    public static void main(String[] args) {
        /*
        type Car {...} ...
        type SportsCar extends Car {...} ...
        decorator CarDecorator decorates Car ...

        SportsCar car1 = SportsCar.new();
        Car car2 = car1;
        car2 add decorator CarDecorator.new();
         */


        Car car = Car._new();

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
        muffler1.deleteDecorator();
        car.gas();
        car.brake(0);

        System.out.println();
        System.out.println("------- delete muffler 2 -----------");
        muffler2.deleteDecorator();
        car.gas();
        car.brake(0);
    }
}
