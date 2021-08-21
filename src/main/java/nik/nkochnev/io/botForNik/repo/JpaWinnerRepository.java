package nik.nkochnev.io.botForNik.repo;

import nik.nkochnev.io.botForNik.model.Winner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaWinnerRepository extends JpaRepository<Winner, Integer> {

    List<Winner> findWinnerByPrized(boolean prized);
}
