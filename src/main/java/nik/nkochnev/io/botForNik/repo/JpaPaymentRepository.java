package nik.nkochnev.io.botForNik.repo;

import nik.nkochnev.io.botForNik.model.Payment;
import nik.nkochnev.io.botForNik.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByUser(User user);

    List<Payment> findPaymentByPaymentDateBetween(LocalDateTime paymentDateStart, LocalDateTime paymentDateEnd);
}
