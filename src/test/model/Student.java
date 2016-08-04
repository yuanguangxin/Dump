package test.model;

public class Student {
//    private int id;
    private String name;

//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public int getId() {
//        return id;
//    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public void Speak(String msg) {
        System.out.println(msg + ",我是" + this.name);
    }
}
