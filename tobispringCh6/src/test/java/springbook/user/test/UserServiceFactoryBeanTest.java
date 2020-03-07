package springbook.user.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import springbook.user.dao.UserDao;
import springbook.user.domain.User;
import springbook.user.domain.User.UserLevel;
import springbook.user.service.TxProxyFactoryBean;
import springbook.user.service.UserService;
import springbook.user.service.UserServiceImpl;


/**
 *   데이터 update중에 에러가 발생했을때, 이전 데이터는 roll-back이 되는지, 그대로 commit이 되는지 
 *   확인하기 위한 테스트 
*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")
public class UserServiceFactoryBeanTest {
	//컨테이너가 관리하는 스프링 빈 선언
	//타입으로 검색, 같은 타입의 빈이 두개라면 필드 이름을 이용해서 찾음. 

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private ApplicationContext context;

	
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;
	
	//픽스쳐 선언 
	List<User> users;
	
	@Before
	public void setUp() {
		//배열을 리스트로 만들어주는 메소드 배열.
		users = Arrays.asList(
			new User ("bumjin", "박범진", "p1", UserLevel.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "sayllclubs.naver.com"),
			new User ("joytouch", "강명성", "p2", UserLevel.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0,"sayllclubs.naver.com"),
			new User ("erwins", "신승한", "p1", UserLevel.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1,"sayllclubs.naver.com"),
			new User ("madnite1", "박범진", "p1", UserLevel.SILVER, 60, MIN_RECOMMEND_FOR_GOLD,"sayllclubs.naver.com"),
			new User ("green", "박범진", "p1", UserLevel.GOLD, 100, Integer.MAX_VALUE,"sayllclubs.naver.com")
		);
	}
	
	@Test
	/* 다이나믹 프록시 팩토리 빈을 직접 만들어서 사용할 때는 없앴다가 다시 등장한 컨텍스트 무효화 애노테이션 
	 * 원래는 applicationContext.xml에서 ProxyFactoryBean에 의해서 TxProxyFactoryBean의 getObject()의 의해
	 * 생성된 dynamic proxy object가 생성되어야 하는데 이러면 target설정을 할수 없어서 이를 무시하고 
	 * ProxyFactoryBean 자체를 가져오기 위해 선언.
	*/
	@DirtiesContext 
	public void upgradeAllOrNothing() throws Exception {
		
		//예외를 발생시킬 사용자의 id를 넣어서 테스트용 UserService 대역 오브젝트를 생성한다.
		TestUserService testUserSerivce = new TestUserService(users.get(3).getId());
		testUserSerivce.setUserDao(this.userDao);//useDao를 수동 DI
		
		TxProxyFactoryBean txProxyFactoryBean = context.getBean("&userService",TxProxyFactoryBean.class);
		txProxyFactoryBean.setTarget(testUserSerivce);
		//변경된 target 설정을 이용해서 트랜잭션 다이나믹 프록시 오브젝트를 다시 생성.
		UserService txUserService = (UserService)txProxyFactoryBean.getObject();
		
		userDao.deleteAll();
		
		for(User user : users)  userDao.add(user); 
		
		try {
			//TestUserSerivce는 업그레이드 작업중에 예외가 발생해야 함. 정상 종료라면 문제 있으니 실패.
			txUserService.upgradeLevels();
			fail("TestUserSerivceException expected"); //JUnit 테스트 결과를 무조건 fail로 함 ( ex. java.lang.AssertionError:message )
		}catch(TestUserServiceException e) {
			//TestUserSerivce가 던져주는 예외를 잡아서 계속 진행되게 한다. 그외는 실패처리 
		}
		//예외가 발생하기전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 체크함.
		checkLevelUpgrade(users.get(1),false);
	}
	
	
	
	/**  테스트를 위한 UserService 대역 생성 
	 *         ※  보통 오버라이딩을 하기 위해 class파일을 별도로 만들어서 상속하는데 테스트용이라면 다음과 같이 inner class중의 한
	 *     종류인 nested class를 이용하면 편함. 
	 *        
	 */
	static class TestUserService extends UserServiceImpl{
		private String id;
		
		//예제를 발생시킬 User 오브젝트의 id를 지정할 수 있게 만든다.
		private TestUserService(String id) {
			this.id = id;
		}

		//UserService의 메소드 오버라이딩
		@Override
		protected void upgradeLevel(User user) { 
			//지정된 id의 User 오브젝트가 발견되면 예외를 던져서 강제로 작업을 중지시킨다.
			System.out.println("this.id >> " + this.id);
			System.out.println("user.getId() : " + user.getId());
			
			if(user.getId().equals(this.id)) throw new TestUserServiceException();
			
			super.upgradeLevel(user);
		}

		
		public static void printTest() {
			System.out.println("HERE IS CALL");
		}
	}
	
	static class TestUserServiceException extends RuntimeException{ }
	
	//upgraded - 어떤 레벨로 바뀔 것인가가 아니라, 다음 레벨로 업그레이드 될것인가 아닌가를 지정.
	private void checkLevelUpgrade(User user, boolean upgraded) {
		System.out.println("checkLevelUpgrade");
		User userUpdate = userDao.get(user.getId());
		
		if(upgraded) { //업데이트 일어났는지 확인
			assertThat(userUpdate.getUserLevel(),is(user.getUserLevel().nextLevel()));
		}else { //업데이트가 일어나지 않았는지 확인
			assertThat(userUpdate.getUserLevel(),is(user.getUserLevel()));
		}
	}//end checkLevelUpgrade
}





