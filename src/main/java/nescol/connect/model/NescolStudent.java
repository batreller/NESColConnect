package nescol.connect.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class NescolStudent {
    @Id
    private String studentId;
    private String name;
    private String surname;

    @Column(columnDefinition = "boolean default false")
    private Boolean registered;  // must store it to prevent registering 2 accounts for same student but with different passwords
}
