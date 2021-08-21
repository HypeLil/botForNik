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

    @Column(name = "seconds")
    private long seconds;

    @Column(name = "ended")
    private boolean ended;

    @Column(name = "start_sum")
    private int startSum;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "auction"
    )
    private List<Participant> participants;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "auction"
    )
    private List<Winner> winners;

    public Auction() {
        this.startDate = LocalDateTime.now();
        this.seconds = 180;
        this.ended = false;
        this.startSum = 0;
        this.winnerId = 0;
    }

    @Override
    public String toString() {
        return "Auction{" +
                "auctionId=" + auctionId +
                ", startDate=" + startDate +
                ", thingName='" + thingName + '\'' +
                ", winnerId=" + winnerId +
                ", lastBet=" + lastBet +
                ", seconds=" + seconds +
                ", ended=" + ended +
                ", startSum=" + startSum +
                '}';
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
