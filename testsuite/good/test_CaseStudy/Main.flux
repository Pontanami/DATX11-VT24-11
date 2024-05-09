type Main{

}
main(String[] args){
    TodoDisplay td = TodoDisplay.new();
    TaskBoard tb = TaskBoard.todoList(20);

    tb add subscriber td::displayItems;

    for (var int i = 0; i < args.length; i++) {
        String arg = args[i];
        if (arg.matches("\\d")) {
            tb.addScheduledTask(args[i+ 1], Long.parseLong(arg));
            i++;
        } else {
            tb.addTask(arg);
        }
    }
}