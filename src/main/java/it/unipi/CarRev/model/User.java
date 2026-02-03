package it.unipi.CarRev.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Document(collection = "users")
@CompoundIndex(def="{'reviews._id':1}",name="index_for_embedded_reviews_in_users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private List<org.bson.Document> reviews;
    private List<ObjectId>otherReviews;

    private String passwordHash;
    private boolean isAdmin;
    private Instant createdAt = Instant.now();

    public User() {}

    public User(String username, String email, String passwordHash, boolean isAdmin) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.isAdmin = isAdmin;
        this.createdAt = Instant.now();
    }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isAdmin() { return isAdmin; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
