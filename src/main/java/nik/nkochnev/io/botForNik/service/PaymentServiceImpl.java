package nik.nkochnev.io.botForNik.service;

import lombok.RequiredArgsConstructor;
import nik.nkochnev.io.botForNik.model.Payment;
import nik.nkochnev.io.botForNik.model.User;
import nik.nkochnev.io.botForNik.repo.JpaPaymentRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl {

    private final JpaPaymentRepository paymentRepository;

    public Payment save(Payment payment){
        payment.setPaymentDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public List<Payment> findByUser(User user){
        return paymentRepository.findByUser(user);
    }

    public List<Payment> findPaymentByPaymentDate(LocalDateTime paymentDateStart, LocalDateTime paymentDateEnd){
         return paymentRepository.findPaymentByPaymentDateBetween(paymentDateStart, paymentDateEnd);
    }

}
