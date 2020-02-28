package springbook.user.service;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/** Spring이 제공하는 ProxyFactoryBean 적용 */

//MethodInterceptor - spring advice interface 구현
public class TransactionAdvice implements MethodInterceptor{
	
	private PlatformTransactionManager transactionManager; //트랜잭션 기능을 제공하는데 필요한 트랜잭션 매니저
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	//MethodInvocation - target을 호출하는 기능을 가진 콜백오브젝트를 프록시로부터 받는다. 덕분에 advice는 특정 타켓에 의존하지 않고 재 사용가능.
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// TODO Auto-generated method stub
		
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			// 콜백을 호출해서 타켓의 메소드를 실행한다. 타켓메소드 호출 전후로, 필요한 부가기능을 넣을 수 있다.
			Object ret = invocation.proceed();
			this.transactionManager.commit(status);
			return ret;
		}catch(RuntimeException e) {
			this.transactionManager.rollback(status);
			throw e;
		}
	}


	
}
