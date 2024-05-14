package Models;

import java.util.ArrayList;
import java.util.List;

import Models.TaskToDo;

public class Child {
    private String name;
    private int coinsBalance;
    private List<TaskToDo> tasks;



    public Child() {
        tasks = new ArrayList<>();
    }

    public Child(String name, int coinsBalance) {
        this.name = name;
        this.coinsBalance = coinsBalance;
        tasks = new ArrayList<>();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCoinsBalance() {
        return coinsBalance;
    }

    public void setCoinsBalance(int coinsBalance) {
        this.coinsBalance = coinsBalance;
    }

    public List<TaskToDo> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskToDo> tasks) {
        this.tasks = tasks;
    }

    public void addTask(TaskToDo task) {
        tasks.add(task);
    }

    public void removeTask(TaskToDo task) {
        tasks.remove(task);
    }

    public static Child getChildFromList(List<Child> children, String childName) {
        for (Child child : children) {
            if (child.getName().equals(childName)) {
                return child;
            }
        }
        return null;
    }
}
