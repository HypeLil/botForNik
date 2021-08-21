package nik.nkochnev.io.botForNik.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "winners")
public class Winner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int winnerId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    private boolean prized;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Winner)) return false;
        Winner winner = (Winner) o;
        return winnerId == winner.winnerId && prized == winner.prized && Objects.equals(user, winner.user) && Objects.equals(auction, winner.auction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(winnerId, user, auction, prized);
    }
}
