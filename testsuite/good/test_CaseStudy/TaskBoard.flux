type TaskBoard publishes TodoItem[] {
    void addTask(String text);
    void addScheduledTask(String text, long deadline);
    void checkTask(int index);
    void uncheckTask(int index);
}
constructors{
    todoList(int size){
        list = TodoItem[].ofSize(size);
    }
}
attributes{
    TodoItem[] list;
    int currentSize = 0;
}
methods{
    void addTask(String text) {
        TodoItem td = TodoItem.newTask(text);
        list[currentSize++] = td;
    }
    void addScheduledTask(String text, long deadline){
         TodoItem td = TodoItem.newTask(text);
         td add decorator ScheduledItem.new(deadline);
         list[currentSize++] = td;
    }
}