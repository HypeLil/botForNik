package nik.nkochnev.io.botForNik.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nik.nkochnev.io.botForNik.model.User;
import nik.nkochnev.io.botForNik.service.UserServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentRestController {

    private final UserServiceImpl userService;
    private final int merchantId = 100; // id магазина

    @PostMapping("/getpay")
    public String getPay(@RequestBody JsonDto jsonDTO) {
        if (jsonDTO == null){
            return "NOT";
        }

        log.info("Операция {}", jsonDTO);

        Optional<User> user = userService.findById(jsonDTO.merchant_order_id);

        if (user.isEmpty()) return "NOT";

        int sum = jsonDTO.getAmount();
        String label = jsonDTO.getSign();

        if (!getMd5(sum, user.get().getUserId()).equalsIgnoreCase(label)){
            return "NOT";
        }

        User u = user.get();
        u.setMoney(sum);
        userService.save(u);

        log.info("Операция {} успешна", jsonDTO);
        return "OK";
    }

    // обработка подписи freeKassa
    @SneakyThrows
    private String getMd5(int sum, int userId){
        String secretWord = "12345godiblack1234";
        String label = merchantId + ":" + sum +":" + secretWord + ":" + userId;
        return DigestUtils.md5Hex(label);
    }
}
