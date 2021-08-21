package nik.nkochnev.io.botForNik.repo;

import nik.nkochnev.io.botForNik.model.Auction;
import nik.nkochnev.io.botForNik.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaParticipantRepository extends JpaRepository<Participant, Integer> {

    @Query("SELECT p FROM Participant p WHERE p.auction = :auc")
    List<Participant> findByAuction(@Param("auc") Auction auction);

    @Query(nativeQuery = true, value = "SELECT * FROM participants p\n" +
            "            WHERE p.auction_id = 2\n" +
            "            ORDER BY p.bet_time DESC LIMIT 1")
    Participant findByAuctionLeader(@Param("auc") Auction auction);

    @Query("SELECT p FROM Participant p " +
            "WHERE p.user.userId = :id " +
            "AND p.auction = :auc")
    List<Participant> findByUserIdAndAuction(@Param("auc") Auction auction, @Param("id") int userId);
}
