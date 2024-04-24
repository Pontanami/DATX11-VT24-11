type GetterMethod{
    String getText();
    float getPreciseNumber();
    boolean getStatus();
}
attributes{
    var String text as getText;
    int number = 3, number2 = 8;
    float ps = 5.5 as getPreciseNumber;
    var boolean status as getStatus;
}