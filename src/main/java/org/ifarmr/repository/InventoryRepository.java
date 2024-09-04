package org.ifarmr.repository;

import org.ifarmr.entity.Inventory;
import org.ifarmr.entity.User;
import org.ifarmr.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Boolean existsByNameAndItemType(String name, ItemType itemType);

    int countByUser(User user);

    List<Inventory> findByUserIdOrderByDateAcquiredDesc(Long userId);

}
