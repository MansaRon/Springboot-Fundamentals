package co.za.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Document(collection = "ecommerce.users")
public class User extends Entity {

    /**
     * User full name.
     */
    @NonNull
    @Pattern(regexp = "^[a-zA-Z]*$", message = "First Name must not contain numbers or special characters")
    private String name;

    /**
     * User email address.
     */
    @Email
    @NonNull
    private String email;

    /**
     * User phone number.
     */
    @NonNull
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 digits long")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must contain only numbers")
    private String phone;

    /**
     * User status
     */
    private Status status;

    /**
     * User password used mostly for Counter users
     */
    @NonNull
    @JsonIgnore
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\\\S+$).{8,20}$")
    private String  password;

    @JsonIgnore
    private byte @NonNull [] salt;

    /**
     * User roles assigned to
     */
    @DBRef
    private Set<String> roles;

    public enum Status {
        ACTIVE, SUSPENDED, AWAITING_CONFIRMATION
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
