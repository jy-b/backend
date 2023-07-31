package fastcampus.scheduling.user.repository;

import fastcampus.scheduling.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLogRepository extends JpaRepository<User, Long> {

}