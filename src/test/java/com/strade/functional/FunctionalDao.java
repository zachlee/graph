package com.strade.functional;

import com.strade.dao.GraphDao;
import com.strade.domain.User;
import org.junit.Test;

import java.util.UUID;

public class FunctionalDao {

	GraphDao graphDao = GraphDao.getInstance();

	@Test
	public void createUserAndGetUser() {
		String userId = UUID.randomUUID().toString();
		try {
			User user = new User(userId,
					"username",
					"email",
					"school",
					"type");
			boolean addUser = graphDao.createUser(user);
			assert addUser;

			User returnedUser = graphDao.getUser(userId);
			assert null != returnedUser;
			assert returnedUser.getUuid().equals(userId);
		} finally {
			graphDao.deleteUser(userId);
		}
	}

	@Test
	public void getNonExistingUserReturnsNull() {
		User nonExistentUser = graphDao.getUser("DoesntExist");
		assert null == nonExistentUser;
	}

	@Test
	public void deleteUserTest() {
		String userId = UUID.randomUUID().toString();
		User user = new User(userId,
				"username",
				"email",
				"school",
				"type");
		boolean addUser = graphDao.createUser(user);
		assert addUser;

		User returnedUser = graphDao.getUser(userId);
		assert null != returnedUser;
		graphDao.deleteUser(userId);
		User nullReturn = graphDao.getUser(userId);
		assert null == nullReturn;
	}
}
