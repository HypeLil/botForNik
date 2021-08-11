package nik.nkochnev.io.botForNik.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "payments")
@ToString
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int paymentId;

    @Column(name = "money")
    private double money;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return paymentId == payment.paymentId && Double.compare(payment.money, money) == 0 && Objects.equals(paymentDate, payment.paymentDate) && Objects.equals(user, payment.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, money, paymentDate, user);
    }
}
