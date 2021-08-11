package nik.nkochnev.io.botForNik.repo;

import nik.nkochnev.io.botForNik.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Integer> {
}
