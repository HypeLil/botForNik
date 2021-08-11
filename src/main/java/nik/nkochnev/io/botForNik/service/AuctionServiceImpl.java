package nik.nkochnev.io.botForNik.service;

import lombok.RequiredArgsConstructor;
import nik.nkochnev.io.botForNik.model.Auction;
import nik.nkochnev.io.botForNik.repo.JpaAuctionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl {

    private final JpaAuctionRepository auctionRepository;

    public Auction save(Auction auction){
       return auctionRepository.save(auction);
    }

    public Optional<Auction> findById(Integer id){
        return auctionRepository.findById(id);
    }

    public List<Auction> findAllActualAuctions(){
        return auctionRepository.findAllActualAuctions();
    }
}
