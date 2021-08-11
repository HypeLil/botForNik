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
}
