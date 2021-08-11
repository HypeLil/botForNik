package nik.nkochnev.io.botForNik.repo;

import nik.nkochnev.io.botForNik.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaAuctionRepository extends JpaRepository<Auction, Integer> {

    @Query("SELECT a FROM Auction a WHERE a.ended = false")
    List<Auction> findAllActualAuctions();
}
