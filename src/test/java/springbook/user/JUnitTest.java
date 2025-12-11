package springbook.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JUnitTest.TestConfig.class)
public class JUnitTest {

    @Configuration
    static class TestConfig {
        // 필요하다면 @Bean 메서드를 추가해서 원하는 빈을 정의하면 됨
    }

    @Autowired
    ApplicationContext context; // 테스트 컨텍스트가 매번 주입해 주는 애플리케이션 컨텍스트는 항상 같은 오브젝트인지 테스트로 확인해본다

    static Set<JUnitTest> testObjects = new HashSet<>();
    static ApplicationContext contextObject = null;

    @Test
    @Transactional
    public void test1() {
        assertFalse(testObjects.contains(this));
        testObjects.add(this);

        assertThat(contextObject == null || contextObject == this.context).isTrue();
        contextObject = this.context;
    }

    @Test
    void test2() {
        assertFalse(testObjects.contains(this));
        testObjects.add(this);

        assertTrue(contextObject == null || contextObject == this.context);
        contextObject = this.context;
    }

    @Test
    void test3() {
        assertFalse(testObjects.contains(this));
        testObjects.add(this);

        assertThat(contextObject == null || contextObject == this.context).isTrue();
    }
}
