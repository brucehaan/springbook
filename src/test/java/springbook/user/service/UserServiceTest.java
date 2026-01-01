package springbook.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static springbook.user.domain.Level.*;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
class UserServiceTest {
    @Autowired
    UserServiceImpl userServiceImpl;

    @Autowired
    UserDao userDao;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    MailSender mailSender;

    List<User> users; // 테스트 픽스처

    @BeforeEach
    void setUp() {
        users = Arrays.asList(
                new User("bumjin", "박범진", "p1", BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0),
                new User("joytouch", "강명성", "p2", BASIC, MIN_LOGCOUNT_FOR_SILVER, 0),
                new User("erwins", "신승한", "p3", SILVER, 60, MIN_RECOMMEND_FOR_GOLD - 1),
                new User("madnite1", "이상호", "p4", SILVER, 60, MIN_RECOMMEND_FOR_GOLD),
                new User("green", "오민규", "p5", GOLD, 100, Integer.MAX_VALUE)
            );
    }

    @Test
    void upgradeLevels() {
        // 고립된 테스트에서는 테스트 대상 오브젝트를 직접 생성하면 된다
        UserServiceImpl userService = new UserServiceImpl();

        // 목 오브젝트로 만든 UserDao를 직접 DI 해준다
        MockUserDao mockUserDao = new MockUserDao(this.users);
        userService.setUserDao(mockUserDao);

        // 메일 발송 결과를 테스트할 수 있도록 목 오브젝트를 만들어 userService의 의존 오브젝트로 주입해준다
        // 메일 발송 여부 확인을 위해 목 오브젝트 DI
        MockMailSender mockMailSender = new MockMailSender();
        userService.setMailSender(mockMailSender);

        // 테스트 대상 실행
        userService.upgradeLevels();

        // MockUserDao로부터 업데이트 결과를 가져온다
        List<User> updated = mockUserDao.getUpdated();

        // 업데이트 횟수와 정보를 확인한다
        assertThat(updated.size()).isEqualTo(2);
        checkUserAndLevel(updated.get(0), "joytouch", SILVER);
        checkUserAndLevel(updated.get(1), "madnite1", GOLD);

        // Mock 오브젝트를 이용한 결과 확인
        List<String> request = mockMailSender.getRequests();
        assertThat(request.size()).isEqualTo(2);
        assertThat(request.get(0)).isEqualTo(users.get(1).getEmail());
        assertThat(request.get(1)).isEqualTo(users.get(3).getEmail());
    }

    private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
        assertThat(updated.getId()).isEqualTo(expectedId);
        assertThat(updated.getLevel()).isEqualTo(expectedLevel);
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel().nextLevel());
        } else {
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel());
        }
    }


    private void checkLevel(User user, Level expectedLevel) {
        User userUpdate = userDao.get(user.getId());
        assertThat(userUpdate.getLevel()).isEqualTo(expectedLevel);
    }

    @Test
    void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4); // GOLD 레벨
        User userWithoutLevel = users.get(0); // 레벨이 비어 있는 사용자, 로직에 따라 등록 중에 BASIC 레벨도 설정돼야 한다
        userWithoutLevel.setLevel(null);

        userServiceImpl.add(userWithLevel);
        userServiceImpl.add(userWithoutLevel);

        // DB에 저장된 결과를 가져와 확인한다
        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel()).isEqualTo(userWithLevel.getLevel());
        assertThat(userWithoutLevelRead.getLevel()).isEqualTo(BASIC);
    }

    @Test
    void upgradeAllOrNothing() throws SQLException {
        TestUserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(userDao);
        testUserService.setMailSender(mailSender);

        // 트랜잭션 기능을 분리한 UserServiceTx는 예외 발생용으로 수정할 필요가 없으니 그대로 사용한다
        UserServiceTx txUserService = new UserServiceTx();
        txUserService.setTransactionManager(transactionManager);
        txUserService.setUserService(testUserService);

        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        try {
            // 트랜잭션 기능을 분리한 오브젝트를 통해 예외 발생용 TestUserService가 호출되게 해야 한다.
            txUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        } catch (TestUserServiceException e) {
            System.out.println("TestUserServiceException");
        }
        checkLevelUpgraded(users.get(1), false);
    }

    static class MockMailSender implements MailSender {
        private List<String> requests = new ArrayList<>();

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public void send(SimpleMailMessage mailMessage) throws MailException {
            requests.add(mailMessage.getTo()[0]); // 전송 요청을 받은 이메일 주소를 저장해둔다. 간단하게 첫 번째 수신자 메일 주소만 저장했다.
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {

        }
    }

    static class MockUserDao implements UserDao {

        private List<User> users; // 레벨 업그레이드 후보 User 오브젝트 목록
        private List<User> updated = new ArrayList<>(); // 업그레이드 대상 오브젝트를 저장해둘 목록

        private MockUserDao(List<User> users) {
            this.users = users;
        }

        public List<User> getUpdated() {
            return this.updated;
        }

        // 스텁 기능 제공
        @Override
        public List<User> getAll() {
            return this.users;
        }

        // 목 오브젝트 기능 제공
        @Override
        public void update(User user) {
            updated.add(user);
        }

        @Override
        public void add(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User get(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            return 0;
        }
    }


    @Test
    void mockUpgradeLevels() {
        UserServiceImpl userService = new UserServiceImpl();

        // 다이내믹한 목 오브젝트 생성과 메소드의 리턴 값 설정, 그리고 DI
        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userService.setUserDao(mockUserDao);

        // 리턴값이 없는 메소드를 가진 목 오브젝트는 더욱 간단하게 만들 수 있다
        MailSender mockMailSender = mock(MailSender.class);
        userService.setMailSender(mockMailSender);

        userService.upgradeLevels();

        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel()).isEqualTo(SILVER);
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel()).isEqualTo(GOLD);

        // 파라미터의 내부 정보를 확인해야 하는 경우에 유용
        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertThat(Objects.requireNonNull(mailMessages.get(0).getTo())[0]).isEqualTo(users.get(1).getEmail());
        assertThat(Objects.requireNonNull(mailMessages.get(1).getTo())[0]).isEqualTo(users.get(3).getEmail());
    }
}