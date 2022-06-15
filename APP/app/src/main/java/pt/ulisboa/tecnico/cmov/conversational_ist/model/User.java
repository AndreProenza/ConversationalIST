package pt.ulisboa.tecnico.conversationalist.model;

public class User {

    private String username;
    private String bio;
    private String photo;

    public User() {
        //Does nothing
    }

    public User(String photo) {
        this.photo = photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
