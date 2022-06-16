package pt.ulisboa.tecnico.cmov.conversational_ist.database;

public class NotifyActive {

    private static NotifyActive instance;
    private String active;

    public static NotifyActive getInstance() {
        if(instance == null){
            instance = new NotifyActive();
            instance.setActive("");
        }
        return instance;
    }


    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
