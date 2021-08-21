package nik.nkochnev.io.botForNik.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nik.nkochnev.io.botForNik.model.Auction;
import nik.nkochnev.io.botForNik.model.Participant;
import nik.nkochnev.io.botForNik.repo.JpaParticipantRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public Participant findByAuctionLeader(Auction auction){
        return participantRepository.findByAuctionLeader(auction);
    }
    public List<Participant> findByUserIdAndAuction(Auction auction, int userId){
        return participantRepository.findByUserIdAndAuction(auction, userId);
    }
}
