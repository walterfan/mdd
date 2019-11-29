package com.github.walterfan.potato.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class UserDetailService {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    public Address saveAddress(Address address) {
        Address savedAddress = this.addressRepository.save(address);
        log.info("savedAddress: {}", savedAddress);
        return savedAddress;
    }

    public UserDetail saveUser(UserDetail userDetail) {
        UserDetail savedUser = this.userDetailRepository.save(userDetail);
        log.info("saveUser: {}", savedUser);
        return savedUser;
    }

    public Address getAddress(UUID userId) {
        Address address = addressRepository.findByUserId(userId.toString());
        return address;
    }

    public UserDetail getUser(UUID userId) {
        UserDetail userDetail = userDetailRepository.findByUserId(userId.toString());
        return userDetail;
    }

    public void deleteUser(UUID userId) {
        Address address = this.getAddress(userId);
        addressRepository.deleteById(address.getId());
        UserDetail userDetail = userDetailRepository.findByUserId(userId.toString());
        userDetailRepository.deleteById(userDetail.getId());

    }
}
