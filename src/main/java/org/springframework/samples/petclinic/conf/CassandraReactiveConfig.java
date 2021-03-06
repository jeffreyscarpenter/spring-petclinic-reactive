package org.springframework.samples.petclinic.conf;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.owner.db.OwnerReactiveDao;
import org.springframework.samples.petclinic.owner.db.OwnerReactiveDaoMapperBuilder;
import org.springframework.samples.petclinic.pet.db.PetReactiveDao;
import org.springframework.samples.petclinic.pet.db.PetReactiveDaoMapperBuilder;
import org.springframework.samples.petclinic.vet.db.VetReactiveDao;
import org.springframework.samples.petclinic.vet.db.VetReactiveDaoMapperBuilder;
import org.springframework.samples.petclinic.visit.db.VisitReactiveDao;
import org.springframework.samples.petclinic.visit.db.VisitReactiveDaoMapperBuilder;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.OptionsMap;
import com.datastax.oss.driver.api.core.config.TypedDriverOption;

// TODO: could we provide a link to Spring Data documentation for the configuration properties?
/**
 * Using keys from the `application.yaml` to initialize a session with the driver.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Configuration
public class CassandraReactiveConfig implements CassandraPetClinicSchema {
    
    /** Logger for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraReactiveConfig.class);

    // TODO: should this say "when" or "with"?
    /** using Spring Data keys when autoconfiguration is OK (no AbstractReactiveCassandraConfiguration). **/
   
    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspace;
    
    @Value("${spring.data.cassandra.schema-action}")
    private String schemaAction;
    
    @Value("${spring.data.cassandra.local-datacenter:datacenter1}")
    private String dc;
    
    @Value("${spring.data.cassandra.username}")
    private String username;
    
    @Value("${spring.data.cassandra.password}")
    private String password;
    
    @Value("${spring.data.cassandra.port:9042}")
    private int port;
    
    @Value("#{'${spring.data.cassandra.contact-points}'.split(',')}") 
    private List<String> contactPoints;
    
    @Value("${spring.data.cassandra.astra.enabled:true}")
    private boolean useAstra;
    
    @Value("${spring.data.cassandra.astra.secure-connect-bundle}")
    private String secureConnectBundle;
    
    @Bean
    public CqlSession cqlSession() {
        LOGGER.info("Initializing connection to Cassandra...");
        OptionsMap om = OptionsMap.driverDefaults();
        om.put(TypedDriverOption.SESSION_KEYSPACE, keyspace);
        om.put(TypedDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(20));
        om.put(TypedDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(20));
        om.put(TypedDriverOption.CONNECTION_SET_KEYSPACE_TIMEOUT, Duration.ofSeconds(20));
        om.put(TypedDriverOption.CONTROL_CONNECTION_AGREEMENT_TIMEOUT, Duration.ofSeconds(20));
        om.put(TypedDriverOption.REQUEST_CONSISTENCY, ConsistencyLevel.LOCAL_QUORUM.name());
        
        if (useAstra) {
            LOGGER.info("+ Connection to Datastax Astra:");
            om.put(TypedDriverOption.CLOUD_SECURE_CONNECT_BUNDLE, secureConnectBundle);
            om.put(TypedDriverOption.AUTH_PROVIDER_CLASS, "PlainTextAuthProvider");
            om.put(TypedDriverOption.AUTH_PROVIDER_USER_NAME, username);
            om.put(TypedDriverOption.AUTH_PROVIDER_PASSWORD, password);
        } else {
            LOGGER.info("+ Connection to a Local Cassandra instance {}", contactPoints);
            om.put(TypedDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, dc);
            om.put(TypedDriverOption.CONTACT_POINTS, contactPoints);
            createLocalKeyspace(keyspace, 1);
        }
        
        CqlSession cqlSession = CqlSession.builder().withConfigLoader(DriverConfigLoader.fromMap(om)).build();
        LOGGER.info("[OK] Connection established to keyspace '{}'", keyspace);
        if ("CREATE_IF_NOT_EXISTS".equals(schemaAction)) {
            createSchema(cqlSession);
        }
        return cqlSession;
    }
    
    /**
     * Initialized {@link OwnerReactiveDao} as a Spring Singleton.
     */
    @Bean
    public OwnerReactiveDao ownerDao(CqlSession cqlSession) {
        return new OwnerReactiveDaoMapperBuilder(cqlSession)
            .build().ownerDao(cqlSession.getKeyspace().get());
    }
    
    /**
     * Initialized {@link PetReactiveDao} as a Spring Singleton.
     */
    @Bean
    public PetReactiveDao petDao(CqlSession cqlSession) {
        return new PetReactiveDaoMapperBuilder(cqlSession).build()
                .petDao(cqlSession.getKeyspace().get());
    }
    
    /**
     * Initialized {@link VisitReactiveDao} as a Spring Singleton.
     */
    @Bean
    public VisitReactiveDao visitDao(CqlSession cqlSession) {
        return new VisitReactiveDaoMapperBuilder(cqlSession).build()
                .visitDao(cqlSession.getKeyspace().get());
    }
    
    /**
     * Initialized {@link VetReactiveDaos} as a Spring Singleton.
     */
    @Bean
    public VetReactiveDao vetDao(CqlSession cqlSession) {
        return new VetReactiveDaoMapperBuilder(cqlSession).build()
                .vetDao(cqlSession.getKeyspace().get());
    }
    
    
    /**
     * Use to create the keyspace if needed. (Not in Astra)
     * 
     * @param keyspaceName
     *      current keyspace name
     * @param replicationFactor
     *      replication factor
     */
    private void createLocalKeyspace(String keyspaceName, int replicationFactor) {
        try (CqlSession cqlSession = CqlSession.builder()
                .addContactPoints(contactPoints
                        .stream().map(cp -> new InetSocketAddress(cp, port))
                        .collect(Collectors.toList()))
                .withLocalDatacenter(dc).build()) {
            createKeyspaceSimpleStrategy(cqlSession, keyspaceName, replicationFactor);
            LOGGER.info("+ Keyspace '{}' created (if needed).", keyspace);
        }
    }
}
