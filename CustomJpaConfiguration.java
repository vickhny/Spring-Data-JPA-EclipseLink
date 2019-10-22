import org.eclipse.persistence.config.BatchWriting;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.logging.SessionLog;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Marc Nuri on 2015-12-05.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories()
public class CustomJpaConfiguration extends JpaBaseConfiguration  {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final EntityManagerFactoryBuilder builder) {
        final LocalContainerEntityManagerFactoryBean ret = builder
                .dataSource(dataSource())
                .packages("com.example.model")
                .persistenceUnit("YourPersistenceUnitName")
                .properties(initJpaProperties()).build();
        return ret;
    }

    @Bean
    public DataSource dataSource() {
        //In classpath from spring-boot-starter-web
        final Properties pool = new Properties();
        pool.put("driverClassName", "org.postgresql.Driver");
        pool.put("url", "jdbc:postgresql://example.com:5432/DatabaseName");
        pool.put("username", "user");
        pool.put("password", "password");
        pool.put("minIdle", 1);
        return new org.apache.tomcat.jdbc.pool.DataSourceFactory().createDataSource(pool);
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Override
    protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
        return new EclipseLinkJpaVendorAdapter();
    }

    @Override
    protected Map<String, Object> getVendorProperties() {
        final Map<String, Object> ret = new HashMap<>();
        ret.put(PersistenceUnitProperties.BATCH_WRITING, BatchWriting.JDBC);
        return ret;
    }
}