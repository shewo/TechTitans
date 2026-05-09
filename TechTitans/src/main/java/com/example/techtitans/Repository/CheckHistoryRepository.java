package com.example.techtitans.Repository;
import com.example.techtitans.Entity.CheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CheckHistoryRepository extends JpaRepository<CheckHistory, Long> {
    List<CheckHistory> findByProxyIdOrderByCheckedAtAsc(String proxyId);
}