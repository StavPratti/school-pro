package gr.aueb.cf.schoolapp.model;

import gr.aueb.cf.schoolapp.core.enums.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.security.Principal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements IdentifiableEntity, Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Override
    public String getName() {
        return username;
    }
}
