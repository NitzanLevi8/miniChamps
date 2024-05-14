package Models;

public class TaskToDo {
    private String id;
    private String childId; // Added childId field
    private String name;
    private String description;
    private String day;
    private int hour;
    private int minute;
    private int coinsRewarded;
    private boolean completed;

    public TaskToDo() {
    }

    public TaskToDo(String id, String childId, String name, String description, String day, int hour, int minute, int coinsRewarded, boolean completed) {
        this.id = id;
        this.childId = childId;
        this.name = name;
        this.description = description;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.coinsRewarded = coinsRewarded;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getCoinsRewarded() {
        return coinsRewarded;
    }

    public void setCoinsRewarded(int coinsRewarded) {
        this.coinsRewarded = coinsRewarded;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}