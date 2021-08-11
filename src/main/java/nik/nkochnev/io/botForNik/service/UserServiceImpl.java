package nik.nkochnev.io.botForNik.service;

import lombok.RequiredArgsConstructor;
import nik.nkochnev.io.botForNik.model.User;
import nik.nkochnev.io.botForNik.repo.JpaUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private JpaUserRepository userRepository;

    public User save(User user){
        Optional<User> us = findById(user.getUserId());
        if (us.isPresent()) return us.get();
        userRepository.save(user);
        return findById(user.getUserId()).get();
    }

    public Optional<User> findById(Integer id){
        return userRepository.findById(id);
    }
}
