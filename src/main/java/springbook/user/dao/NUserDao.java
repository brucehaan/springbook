package springbook.user.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 2025-11-29 질문 : N사와 D사에 UserDao 클래스만 공급하고 상속을 통해 DB 커넥션 기능을 확장해서 사용했던 게 다시 불가능해졌다.
 * 왜냐하면 UserDao의 코드가 SimpleConnectionMaker라는 특정 클래스에 종속되어 있기 때문에,
 * 상속을 사용했을 때처럼 UserDao 코드의 수정 없이 DB 커넥션 생성 기능을 변경할 방법이 없다. -> 왜일까? 이해가 안 된다. 토비의 스프링 vol1. 73페이지
 *
 * 두 번째 문제는 DB 커넥션을 제공하는 클래스가 어떤 것인지를 UserDao가 구체적으로 알고 있어야 한다는 점이다.
 * UserDao에 SimpleConnectionMaker라는 클래스 타입의 인스턴스 변수까지 정의해놓고 있으니,
 * N사에서 다른 클래스를 구현하면 어쩔 수 없이 UserDao 자체를 다시 수정해야 한다. -> 토비의 스프링 vol1. 74페이지
 */
//public class NUserDao extends UserDao {
//    public Connection getConnection() throws ClassNotFoundException, SQLException {
//        // N 사 DB Connection 생성코드
//        return null;
//    }
//}
