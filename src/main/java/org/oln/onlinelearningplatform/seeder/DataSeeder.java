package org.oln.onlinelearningplatform.seeder;

import lombok.RequiredArgsConstructor;
import org.oln.onlinelearningplatform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SeederService seederService;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seederService.seedAllData();
            System.out.println(">>> Đã seed 3 users, 10 khóa học và 30 bài học thành công!");
        }
    }
}