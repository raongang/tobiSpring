package springbook.user.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import springbook.user.dao.UserDao;
import springbook.user.domain.User;
import springbook.user.domain.User.UserLevel;
import springbook.user.service.UserService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")

/* 롤백 여부에 대한 기본설정과 트랜잭션 매니저 빈을 지정하는데 사용.
   default 트랜잭션 매니저 아이디는 관례에 따라 transactionManager로 되어 있다.
*/
@Transactional
@Rollback(false)
//@TransactionConfiguration(defaultRollback=false)  spring4.2에서 deprecated

public class UserServiceTest2 {

	//컨테이너가 관리하는 스프링 빈 선언
	//타입으로 검색, 같은 타입의 빈이 두개라면 필드 이름을 이용해서 찾음. 
	@Autowired
	private UserDao userDao;

	/* 같은 타입의 빈이 2개이므로, 필드이름을 기준으로 주입될 빈이 결정된다. 자동 프록시 생성기에 의해 트랜잭션 부가기능이 testUserService 빈에 적용되었는지를 확인하기 위함.*/
	@Autowired
	UserService testUserService;
	
	//테스트를 위해 명시적 트랜잭션 선언
	@Autowired
	PlatformTransactionManager transactionManager;
	@Autowired 
	UserService userService;
	
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
	}//end setup

	/*
	 *   테스트를 위한 트랜잭션 애노테이션
	 *     1.  transactionSyncRollback 처럼 명시적으로 트랜잭션을 만들어서 쓸수도 있지만 Annotation을 이용한 선언적 트랜잭션도 사용가능
	 *       - 테스트 메소드 실행전 새로운 트랜잭션을 만들어주고, 메소드가 종료되면 트랜잭션이 종료된다.
	 *       - deleteAll, add는 메소드의 트랜잭션에 참여
	 *     
	 *     2. @Transactional
	 *     - 테스트에 적용된 @Transactional은 기본적으로 트랜잭션을 강제 롤백하게 설정 되어 있음. 
	 *     
	 *     3. @Rollback ( 메소드레벨)
	 *     - 모두 다 강제롤백하게 설정되어 있으므로, Rollback을 이용 설정가능
	 *     - default값 : true
	 *    
	 *     4. @TransactionConfiguration 
	 *       - 3번 @Rollback과 같은 기능이지만 클래스 레벨에 선언
	 *       
	 *     5. @NotTransactional( spring3.0에서 제거), @Transactional(propagation=Propagation.NEVER)
	 *       -  @Transactional 설정을 무시하고, 트랜잭션을 시작하지 않음.
	 */
	
	@Test
	//@Transactional(readOnly=true)
	//@Rollback(false)  //예외가 발생하지 않는 한 commit
	@Rollback //메소드에서 디폴트설정과 그밖의 롤백방법을 재 설정할수 있음( 우선순위 : 메소드 > 클래스 ) 
	public void trnasactionSyncAnnotation() {
		userService.deleteAll();
		assertThat(userDao.getCount(),is(0));
		userService.add(users.get(0));
		userService.add(users.get(1));
	}

	
	
	
}//end UserServiceTest





