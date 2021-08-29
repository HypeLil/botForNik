package nik.nkochnev.io.botForNik.service;

import lombok.RequiredArgsConstructor;
import nik.nkochnev.io.botForNik.model.User;
import nik.nkochnev.io.botForNik.repo.JpaUserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final JpaUserRepository userRepository;

    public User save(User user){
        userRepository.save(user);
        return findById(user.getUserId()).get();
    }

    public Optional<User> findById(Integer id){
        return userRepository.findById(id);
    }

    public List<User> findAllByLastActionBetween(LocalDateTime lastActionStart, LocalDateTime lastActionEnd){
        return userRepository.findAllByLastActionBetween(lastActionStart, lastActionEnd);
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }
}
