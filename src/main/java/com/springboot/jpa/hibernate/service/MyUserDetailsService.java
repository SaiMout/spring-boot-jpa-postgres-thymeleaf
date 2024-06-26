package com.springboot.jpa.hibernate.service;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.springboot.jpa.hibernate.model.Role;
import com.springboot.jpa.hibernate.model.User;
import com.springboot.jpa.hibernate.respository.IUserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

	private static final int MAX_FAILED_ATTEMPTS = 3;
	private final IUserRepository userRepository;

	public MyUserDetailsService(IUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public void createUser(User user) {
		userRepository.save(user);

	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		if (!user.getAccountNonLocked()) {
			throw new LockedException("This Account is locked!!");
		} else if (user.getFailedAttemptCount() >= MAX_FAILED_ATTEMPTS) {
			lockUserAccount(user);
			throw new LockedException("Account locked due to too many failed login attempts");

		} else { 
//			reSetFailedAttemptCount(user);
		}

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				user.isEnabled(), true, true, user.getAccountNonLocked(),
				AuthorityUtils.createAuthorityList(user.getRoles().stream().map(Role::getName).toArray(String[]::new)));
	}

	private void lockUserAccount(User user) {
		user.setAccountNonLocked(false);
		userRepository.save(user);
	}

//	private void reSetFailedAttemptCount(User user) {
//		user.setFailedAttemptCount(0);
//		userRepository.save(user);
//	}
}
