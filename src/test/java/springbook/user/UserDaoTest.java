package springbook.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import springbook.user.dao.UserDao;
import springbook.user.domain.User;


import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(locations = "/test-applicationContext.xml")
//@DirtiesContext // 테스트 메소드에서 애플리케이션 컨텍스트의 구성이나 상태를 변경한다는 것을 테스트 컨텍스트 프레임워크에 알려준다
public class UserDaoTest {

    UserDao dao; // UserDao 타입 빈을 직접 DI 받는다

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {

        this.user1 = new User("gyumee", "박성철", "springno1");
        this.user2 = new User("leegw700", "이길원", "springno2");
        this.user3 = new User("bumjin", "박범진", "springno3");

        // 오브젝트 생성, 관계설정 등을 모두 직접 해준다
        // 애플리케이션 컨텍스트가 만들어지는 번거로움은 없어졌지만, 매번 새로운 테스트 오브젝트를 만들어야 하는 단점이 있다
        dao = new UserDao();
        DataSource dataSource = new SingleConnectionDataSource( // 테스트에서 UserDao가 사용할 DataSource 오브젝트를 직접 생성한다.
                "jdbc:mysql://localhost:3306/springbook", "root", "cometrue", true
        );
        dao.setDataSource(dataSource); // 코드에 의한 수동 DI
    }

    @Test
    void addAndGet() throws SQLException, ClassNotFoundException {
        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);

        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount()).isEqualTo(2);

        User userGet1 = dao.get(user1.getId());
        assertThat(userGet1.getName()).isEqualTo(user1.getName());
        assertThat(userGet1.getPassword()).isEqualTo(user1.getPassword());

        User userGet2 = dao.get(user2.getId());
        assertThat(userGet2.getName()).isEqualTo(user2.getName());
        assertThat(userGet2.getPassword()).isEqualTo(user2.getPassword());
    }

    @Test
    void count() throws SQLException {

        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);

        dao.add(user1);
        assertThat(dao.getCount()).isEqualTo(1);

        dao.add(user2);
        assertThat(dao.getCount()).isEqualTo(2);

        dao.add(user3);
        assertThat(dao.getCount()).isEqualTo(3);

    }

    @Test
    void getUserFailure() throws SQLException {
        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);

        assertThatThrownBy(() -> dao.get("unknown_id")).isInstanceOf(EmptyResultDataAccessException.class);
    }
}
