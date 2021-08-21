package nik.nkochnev.io.botForNik.service;

import lombok.RequiredArgsConstructor;
import nik.nkochnev.io.botForNik.model.Winner;
import nik.nkochnev.io.botForNik.repo.JpaWinnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WinnerService {

    private final JpaWinnerRepository winnerRepository;

    public Winner save(Winner winner){
        return winnerRepository.save(winner);
    }

    public Optional<Winner> findById(Integer id){
        return winnerRepository.findById(id);
    }

    public List<Winner> findWinnerByPrized(boolean prized){
        return winnerRepository.findWinnerByPrized(prized);
    }
}
