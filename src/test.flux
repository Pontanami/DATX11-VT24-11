type Car {
    void Test();
}
constructors{
    At(int x, int y){}

    Origin(Car car){}
}
components{
    Car car = Car.new();
    Person person;
}
methods{
    void Test(){
        var int a = 5;
        if(a==4){
            a = 2;
        }

        for(var int i; i<10; i++)
        {
            a--;
        }
    }
}
