package com.s20683.wmphs.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<CompletationOrder, Integer> {

    @Query(value = "SELECT * FROM completation_order WHERE user_id = :userId AND state = 1", nativeQuery = true)
    public List<CompletationOrder> findReleasedToCompletationOrdersPerUser(int userId);

    @Query(value = "SELECT * FROM completation_order WHERE user_id = :userId AND state = 2 LIMIT 1", nativeQuery = true)
    public Optional<CompletationOrder> findOrderInCompletationPerUser(int userId);
}
