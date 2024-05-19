type TodoDisplay {
    void displayItems(TodoItem[] items);
}
methods {
    void displayItems(TodoItem[] items) {
        System.out.println("------------ To do ---------------");
        for (var int i = 0; i < items.length; i++) {
            TodoItem item = items[i];
            System.out.println("item #" + (i+1) + ":\n" + item.getText());
        }
        System.out.println("");
    }
}