package nik.nkochnev.io.botForNik.service;

import lombok.RequiredArgsConstructor;
import nik.nkochnev.io.botForNik.model.Auction;
import nik.nkochnev.io.botForNik.model.Participant;
import nik.nkochnev.io.botForNik.repo.JpaParticipantRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl {

    private final JpaParticipantRepository participantRepository;

    public void save(Participant participant){
        participant.setBetTime(LocalDateTime.now());
        participantRepository.save(participant);
    }

    public Optional<Participant> findById(Integer id){
        return participantRepository.findById(id);
    }

    public List<Participant> findByAuction(Auction auction){
        return participantRepository.findByAuction(auction);
    }

    public boolean update(Integer id, double sum){
        Optional<Participant> byId = findById(id);
        if (byId.isEmpty()) return false;
        Participant participant = byId.get();
        participant.setBetMoney(sum);
        save(participant);
        return true;
    }

    public List<Participant> findByAuctionLeader(@Param("auc") Auction auction){
        return participantRepository.findByAuctionLeader(auction);
    }
    public Participant findByUserIdAndAuction(Auction auction, int userId){
        return participantRepository.findByUserIdAndAuction(auction, userId);
    }
}
