package co.za.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "users")
public class User extends Entity {

    /**
     * User full name.
     */
    @NonNull
    private String name;

    /**
     * User email address.
     */
    @NonNull
    private String email;

    /**
     * User phone number.
     */
    @NonNull
    private String phone;

    /**
     * User status
     */
    private Status status;

    /**
     * User code.
     */
    private String code;

    /**
     * User password used mostly for Counter users
     */
    @JsonIgnore
    private byte @NonNull [] password;

    @JsonIgnore
    private byte @NonNull [] salt;

    /**
     * User roles assigned to
     */
    @DBRef
    private Set<String> roles;

    public enum Status {
        ACTIVE,SUSPENDED,AWAITING_CONFIRMATION
    }

    public void addRoles(String... newRoles) {
        if (roles == null) roles = new HashSet<>();
        roles.addAll(Arrays.asList(newRoles));
    }

    public void removeRoles(String... newRoles) {
        if (roles == null) return;
        Arrays.asList(newRoles).forEach(roles::remove);
    }

}
