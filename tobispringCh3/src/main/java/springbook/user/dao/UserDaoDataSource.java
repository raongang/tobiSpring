package springbook.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import springbook.user.domain.User;

public class UserDaoDataSource {
	
	// 인터페이스 도입으로 인해 UserDao는 자신이 사용할 클래스가 어떤 건지 몰라도 됨. 인터페이스를 통해 원하는 기능을 사용하기만 하면 된다.
	/* 1.6.2 싱글톤과 오브젝트 상태
	 *  1) 멀티 스레드 환경에서 기본적으로 인스턴스 필드 값을 변경하고 유지하는 상태유지(stateful)방식으로 만들지 않는다.
	    2) 아래와 같이 선언하면 멀티스레드 환경에서 매번 새로운 값으로 바뀌기 때문에 심각한 문제가 발생
	        - 파라미터와 로컬변수, 리턴값을 이용하면 새로운 값을 저장할 독립적인 공간이 만들어지기 때문에 싱글톤이라고 해도 여러 스레드가 변수를 덮어쓰지 않음.
	        
	private Connection c;
	private User user;
	*/
	
	private DataSource dataSource;
	
	
	 public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}



	public void add(User user) throws  SQLException, ClassNotFoundException{
		
		Connection conn =  dataSource.getConnection(); 
		PreparedStatement ps = conn.prepareStatement("insert into users(id,name,password) values(?,?,?)");
		
		ps.setString(1, user.getId());
		ps.setString(2, user.getName()); 
		ps.setString(3, user.getPassword());
		
		ps.executeUpdate();
		
		ps.close();
		conn.close();
	 }//end add


	
	public User get(String id) throws ClassNotFoundException, SQLException{
		Connection conn =  dataSource.getConnection();  
		
		PreparedStatement ps = conn.prepareStatement("select * from users where id=?");
		ps.setString(1, id);
		
		ResultSet rs = ps.executeQuery();
		rs.next();
		
		User user = new User();
		user.setId(rs.getString("id"));
		user.setName(rs.getString("name"));
		user.setPassword(rs.getString("password"));
		
		rs.close();
		ps.close();
		conn.close();
		return user;
	 }//end get
	

}
