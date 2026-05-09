package com.example.techtitans.Repository;
import com.example.techtitans.Entity.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< Updated upstream
import com.example.techtitans.Entity.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProxyRepository extends JpaRepository<Proxy, String> {
    //  "Proxy" means your Entity class, "String" means Primary Key (proxyId) type
}
=======
public interface ProxyRepository extends JpaRepository<Proxy, String> {}
>>>>>>> Stashed changes
