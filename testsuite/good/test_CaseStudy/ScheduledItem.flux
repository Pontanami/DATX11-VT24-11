decorator ScheduledItem decorates TodoItem
constructors {
    new(String deadline) {
        this.deadline = deadline;
    }
}
attributes {
    String deadline;
}
methods {
    String getText() {
        return "deadline: " + deadline + "\ndescription: " + base.getText();
    }
}