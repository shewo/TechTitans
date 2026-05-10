package com.example.techtitans.Repository;
import com.example.techtitans.Entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, String> {}