decorator ScheduledItem decorates ToDoItem
constructors {
    new(long deadline) {
        this.deadline = deadline;
    }
}
attributes {
    long deadline;
}
methods {
    String getText() {
        return "deadline: " + deadline + "\ndescription: " + base.getText();
    }
}