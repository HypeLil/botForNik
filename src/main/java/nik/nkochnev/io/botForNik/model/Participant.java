package nik.nkochnev.io.botForNik.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "participants")
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int participantId;

    @Column(name = "bet_money", nullable = false)
    private double betMoney;

    @Column(name = "bet_time")
    private LocalDateTime betTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participant)) return false;
        Participant that = (Participant) o;
        return participantId == that.participantId && Double.compare(that.betMoney, betMoney) == 0 && Objects.equals(user, that.user) && Objects.equals(auction, that.auction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participantId, betMoney, user, auction);
    }
}
