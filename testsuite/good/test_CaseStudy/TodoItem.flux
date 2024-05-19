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
    String getText() {
        String status;
        if (done) status = "completed";
        else status = "not completed";
        return text + "(" + status + ")";
    }
    void check() { done = true; }
    void uncheck() { done = false; }
}