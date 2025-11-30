package springbook.user.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoFactory {

    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao();
        userDao.setConnectionMaker(connectionMaker());
        return userDao;
    }

    // 분리해서 중복을 제거한 ConnectionMaker 타입 오브젝트 생성 코드
    @Bean
    public ConnectionMaker connectionMaker() {
        return new DConnectionMaker();
        // return new LocalDBConnectionMaker();
        // return new ProductionDBConnectionMaker();
    }
}
