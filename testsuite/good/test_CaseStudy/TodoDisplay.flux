type TodoDisplay {
    void displayItems(TodoItem[] items);
}
methods {
    void displayItems(TodoItem[] items) {
        for (int i = 0; i < items.length; i++) {
            System.out.println("item #" + i + ":\n" + item.getText());
        }
    }
}