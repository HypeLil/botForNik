package nik.nkochnev.io.botForNik.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class User {

    @Id
    private int userId;

    @Column(name = "money")
    private double money;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "email")
    private String email;

    @Column(name = "position")
    private String position;

    @Column(name = "username")
    private String username;

    @Column(name = "last_action")
    private LocalDateTime lastAction;

    public User(int userId) {
        this.userId = userId;
        this.regDate = LocalDateTime.now();
        this.money = 0;
        this.position = "start";
    }

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "user"
    )
    private List<Payment> payments;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "user"
    )
    private List<Participant> participants;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "user"
    )
    private List<Winner> winners;

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", money=" + money +
                ", regDate=" + regDate +
                ", email='" + email + '\'' +
                ", position='" + position + '\'' +
                ", username='" + username + '\'' +
                ", lastAction=" + lastAction +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId == user.userId && Objects.equals(regDate, user.regDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, regDate);
    }
}
