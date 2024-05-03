type TodoItem{
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
    String text;
}