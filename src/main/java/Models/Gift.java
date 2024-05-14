// Assuming Gift.java is your model class
package Models;

public class Gift {
    private String id;
    private String name;
    private String description;
    private int coinsValue;

    public Gift(String id, String name, String description, int coinsValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.coinsValue = coinsValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getCoinsValue() {
        return coinsValue;
    }

    public void setCoinsValue(int coins) {
        this.coinsValue = coins;
    }
}
