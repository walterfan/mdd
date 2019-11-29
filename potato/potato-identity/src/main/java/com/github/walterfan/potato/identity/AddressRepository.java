package com.github.walterfan.potato.identity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    Address findByUserId(String userId);
}

