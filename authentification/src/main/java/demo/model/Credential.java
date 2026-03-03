package demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "credentials")
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This stores the actual role name, e.g., "ROLE_ADMIN", "ROLE_USER"
    @Column(unique = true, nullable = false)
    private String name;

    public Credential() {
    }

    public Credential(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}