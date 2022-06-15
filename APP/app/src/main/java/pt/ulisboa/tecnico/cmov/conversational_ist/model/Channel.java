package pt.ulisboa.tecnico.cmov.conversational_ist.model;

public class Channel {

    private String id;
    private String name;

    public Channel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

}
