package org.ifarmr.repository;

import org.ifarmr.entity.Crop;
import org.ifarmr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {

    Optional<Crop> findByCropName(String name);

    List<Crop> findByUser_Username(String username);

    int countByUser(User user);

    List<Crop> findByUserIdOrderBySowDateDesc(Long userId);

}
