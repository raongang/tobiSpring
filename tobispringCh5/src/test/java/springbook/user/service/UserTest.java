package springbook.user.service;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import springbook.user.dao.UserDao;
import springbook.user.domain.User;
import springbook.user.domain.User.UserLevel;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")
public class UserTest {
 
	User user;
	
	@Before
	public void setUp() {
		user = new User();
	}
	
	@Test
	public void upgradeLevel() {
		UserLevel[] levels = UserLevel.values();
		for(UserLevel level : levels) {
			
			//System.out.println("level : " + level);
			//System.out.println("level.nextLevel() : " + level.nextLevel());
			
			if(level.nextLevel() == null) continue;
			user.setUserLevel(level);
			user.upgradeLevel();
			
			//System.out.println("결과 : " + user.getUserLevel() + " " + level.nextLevel());
			assertThat(user.getUserLevel(),is(level.nextLevel()));
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void cannotUpGradeLevel() {
		
		UserLevel[] levels = UserLevel.values();
		
		for(UserLevel level : levels) {
			
			//System.out.println("level : " + level);
			//System.out.println("level.nextLevel() : " + level.nextLevel());
			
			if(level.nextLevel() != null) continue;
			user.setUserLevel(level);
			user.upgradeLevel();
			
			//System.out.println("결과 : " + user.getUserLevel() + " " + level.nextLevel());
			
		}
	}
	
	
	
	
	
}
