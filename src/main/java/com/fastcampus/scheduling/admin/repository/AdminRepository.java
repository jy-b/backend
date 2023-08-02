package com.fastcampus.scheduling.admin.repository;

import com.fastcampus.scheduling.schedule.model.Schedule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Schedule, Long> {


}
