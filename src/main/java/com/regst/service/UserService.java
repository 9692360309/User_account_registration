package com.regst.service;

import java.util.Optional;

import com.regst.entity.User;

public interface UserService {
	 Optional<User> findByEmail(String email);
	    void saveUser(User user);
	

}
