package org.ifarmr.repository;

import org.ifarmr.entity.Crop;
import org.ifarmr.entity.LiveStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LiveStockRepository extends JpaRepository<LiveStock, Long> {

    Optional<LiveStock> findByAnimalName(String name);

    List<LiveStock> findByUser_Username(String username);

    List<LiveStock> findByUserIdOrderByDateCreatedDesc(Long userId);

}
