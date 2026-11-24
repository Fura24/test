package com.example.auth.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One row of the "roles" table, e.g. ROLE_USER or ROLE_ADMIN. */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    private Short id;

    @Column(nullable = false, unique = true, length = 32)
    private String name;

    protected Role() {
        // required by JPA
    }

    public Short getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
