package org.ifarmr.service;

import org.ifarmr.entity.Inventory;
import org.ifarmr.entity.User;
import org.ifarmr.payload.request.UserDetailsDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDetailsDto editUserDetails(String username, UserDetailsDto userDetailsDto, MultipartFile file);



    @Transactional
    List<Inventory> getInventoryForUser(User authenticatedUser);

    void updateLastActive(String username);
}
