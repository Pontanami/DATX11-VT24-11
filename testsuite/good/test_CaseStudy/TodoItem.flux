decorable type TodoItem{
   void check();
   void uncheck();
   String getText();
}
constructors
{
    newTask(String text){
        this.text = text;
    }
}
attributes{
    var boolean done;
    String text;
}
methods {
    String getText() { return text; }
    void check() { done = true; }
    void uncheck() { done = false; }
}