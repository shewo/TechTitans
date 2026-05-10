package com.example.techtitans.Repository;

import com.example.techtitans.Entity.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProxyRepository extends JpaRepository<Proxy, String> {

    // You literally don't need to write anything inside here!
    // Spring gives you save(), findAll(), findById(), and deleteById() for free.
    // And because of your Soft Delete entity, deleted proxies stay hidden automatically.

}