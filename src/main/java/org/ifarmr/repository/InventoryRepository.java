package org.ifarmr.repository;

import org.ifarmr.entity.Inventory;
import org.ifarmr.entity.User;
import org.ifarmr.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Boolean existsByNameAndItemType(String name, ItemType itemType);

    int countByUser(User user);

    List<Inventory> findByUserIdOrderByDateAcquiredDesc(Long userId);

    // calculates the total inventory quantity.
    @Query("SELECT SUM(CAST(i.quantity AS long)) FROM Inventory i")
    Long findTotalInventory();

    // calculates the total inventory value
    @Query("SELECT SUM(CAST(i.cost AS long) * CAST(i.quantity AS long)) FROM Inventory i")
    Long findTotalInventoryValue();

}
