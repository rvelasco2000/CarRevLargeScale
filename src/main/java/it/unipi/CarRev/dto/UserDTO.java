package it.unipi.CarRev.dto;

public class UserDTO {
    private String id;
    private String username;
    private String email;
    private boolean isAdmin;

    public UserDTO() {}

    public UserDTO(String id, String username, String email, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isAdmin() { return isAdmin; }

    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}
