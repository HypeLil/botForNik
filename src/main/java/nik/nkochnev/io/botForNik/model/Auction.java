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
@Getter
@Setter

@Table(name = "auctions")
@ToString
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int auctionId;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "thing_name" , nullable = false)
    private String thingName;

    @Column(name = "winner_id")
    private int winnerId;

    @Column(name = "last_bet")
    private LocalDateTime lastBet;

    @Column(name = "ended")
    private boolean ended;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "auction"
    )
    private List<Participant> participants;

    public Auction() {
        startDate = LocalDateTime.now();
        ended = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Auction)) return false;
        Auction auction = (Auction) o;
        return auctionId == auction.auctionId && winnerId == auction.winnerId && Objects.equals(startDate, auction.startDate) && Objects.equals(thingName, auction.thingName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionId, startDate, thingName, winnerId);
    }
}
