type TaskBoard publishes TodoItem[] {
    void addTask(String text);
    void addScheduledTask(String text, String deadline);
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
    var int currentSize = 0;
}
methods{
    void addTask(String text) {
        TodoItem td = TodoItem.newTask(text);
        list[currentSize++] = td;
        publish listCopy();
    }
    void addScheduledTask(String text, String deadline){
         TodoItem td = TodoItem.newTask(text);
         td add decorator ScheduledItem.new(deadline);
         list[currentSize++] = td;
         publish listCopy();
    }
    void checkTask(int index) {
        if (index < currentSize) {
            TodoItem item = list[index];
            item.check();
        }
    }
    void uncheckTask(int index) {
        if (index < currentSize) {
            TodoItem item = list[index];
            item.check();
        }
    }

    TodoItem[] listCopy() {
        TodoItem[] newList = TodoItem[].ofSize(currentSize);
        for (var int i = 0; i < currentSize; i++) {
            newList[i] = list[i];
        }
        return newList;
    }
}