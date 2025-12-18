package springbook.user.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

@Configuration
public class DaoFactory {

    @Bean
    public UserDaoJdbc userDao() {
        UserDaoJdbc userDao = new UserDaoJdbc();
        userDao.setDataSource(dataSource());
        return userDao;
    }

    // 분리해서 중복을 제거한 ConnectionMaker 타입 오브젝트 생성 코드
    @Bean
    public ConnectionMaker connectionMaker() {
        return new DConnectionMaker();
        // return new LocalDBConnectionMaker();
        // return new ProductionDBConnectionMaker();
    }

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();

//        dataSource.setDriverClass(Driver.class);
//        dataSource.setUrl("jdbc:mysql://localhost:3306/springbook");
//        dataSource.setUsername("root");
//        dataSource.setPassword("cometrue");

        return dataSource;
    }
}
