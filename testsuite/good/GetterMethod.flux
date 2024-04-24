type GetterMethod{
    string getText();
    float getPreciseNumber();
    bool getStatus();
}
attributes{
    var string text as getText;
    int number = 3, number2 = 8;
    float ps = 5.5 as getPreciseNumber;
    var bool status as getStatus;
}