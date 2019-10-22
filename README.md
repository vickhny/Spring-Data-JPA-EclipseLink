# Spring Data JPA + EclipseLink: Configuring Spring-Boot to use EclipseLink as the JPA provider 

By default Spring uses Hibernate as the default JPA vendor. Although Hibernate is a good choice, some of us may prefer to use EclipseLink as it was supposed to be the reference implementation for the Java Persistence JSR.

In this tutorial we will setup a Spring-Boot application to use EclipseLink with a PostgreSQL database, although it can be used with any other database.

# Fixing dependencies
In order to use EclipseLink, we must remove Hibernate’s entity manager from the classpath in order to avoid problems. This is a matter of adding an exclusion to your application’s Gradle script or Maven’s pom.xml file. If you are using some other build tool this step won’t be necessary, or at least it will be different.


dependencies { <br/>
    compile("org.springframework.boot:spring-boot-starter-web") <br/>
    compile("org.springframework.boot:spring-boot-starter-actuator") <br/> 
    compile("org.springframework.boot:spring-boot-starter-data-jpa"){ <br/>
        exclude group: "org.hibernate", module: "hibernate-entitymanager" <br/>
    } <br/>
    //...Your project dependencies... <br/>
} <br/>

In the above code block we are adding the exclusion to a Gradle script.


In order to change the JPA vendor, we are going to extend JpaBaseConfiguration class and annotate it as a Spring @Configuration class.

As we are extending JpaBaseConfiguration, we must implement the createJpaVendorAdapter() and getVendorProperties() methods. The first one must return a subclass of AbstractJpaVendorAdapter, in this case it’s really easy because Spring has an out of the box adapter for EclipseLink (ExclipseLinkJpaVendorAdapter). The second method (getVendorProperties()) must return a Map with the JPA properties for the JPA implementation. You can usually return an empty Map.

To complete the configuration, we are going to set up some custom Beans that will be picked up in the appropriate Autowired fields used by Spring.

The first Bean “entityManagerFactory” is used to build an EntityManagerFactory using the supplied builder and specifying our custom parameters. In this case we specify the default PersistenceUnit name (we have more than one), the dataSource, the package names where our Entity annotated classes are and some extra JPA properties.

We must also define a “transactionManager” bean. In this case we are going to use Spring’s JpaTransactionManager.

Next we define a DataSource for the Persistence provider. In this case we are using the simple DriverManagerDataSource, although in a production environment it’s recommended to use a connection pooled provider.  Here we specify the details for our connection. These parameters will override those specified in the application properties file.

If we are using Spring-Boot with embedded Apache Tomcat server, we can use Tomcat’s container DataSource which has connection pooling capabilities:

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
    
In this case, instead of returning Spring’s DriverManagerDataSource, we are going to use Tomcat’s DataSourceFactory to generate a DataSource with the specified connection pooling parameters. You can find a list of available properties for Tomcat’s DataSource here - https://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html#Common_Attributes , in order to fine tune your database connections.

Finally, when you run your application, Spring-Boot will pick up your configuration and create the EntityManagerFactory using EclipseLink instead of Hibernate.
