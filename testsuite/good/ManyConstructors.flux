type ManyConstructors {
   int getAge();
   String getModel();
}
attributes {
   int age;
   String model;
}
constructors {
    newVolvo() {
        this.age = 0;
        this.model = "volvo";
    }
    usedVolvo(int age) {
        this.age = age;
        this.model = "volvo";
    }
    newSaab() {
        this.age = 0;
        this.model = "saab";
    }
    usedSaab(int age) {
        this.age = age;
        this.model = "saab";
    }
    custom(int age, String model) {
        this.age = age;
        this.model = model;
    }
}
methods {
   int getAge() { return age; }
   String getModel() { return model; }
}
main {
   int usedVolvoAge = 1;
   int usedSaabAge = 2;
   int customAge = 3;
   String customModel = "ford";
   
   ManyConstructors newVolvo = ManyConstructors.newVolvo();
   ManyConstructors newSaab = ManyConstructors.newSaab();
   ManyConstructors usedVolvo = ManyConstructors.usedVolvo(usedVolvoAge);
   ManyConstructors usedSaab = ManyConstructors.usedSaab(usedSaabAge);
   ManyConstructors custom = ManyConstructors.custom(customAge, customModel);

   System.out.println(newVolvo.getAge() == 0);
   System.out.println(newVolvo.getModel().equals("volvo"));

   System.out.println(newSaab.getAge() == 0);
   System.out.println(newSaab.getModel().equals("saab"));

   System.out.println(usedVolvo.getAge() == usedVolvoAge);
   System.out.println(usedVolvo.getModel().equals("volvo"));

   System.out.println(usedSaab.getAge() == usedSaabAge);
   System.out.println(usedSaab.getModel().equals("saab"));

   System.out.println(custom.getAge() == customAge);
   System.out.println(custom.getModel().equals(customModel));
}