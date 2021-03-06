package org.springframework.samples.petclinic.owner.db;


import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

// TODO: add interface comment
@Mapper
public interface OwnerReactiveDaoMapper {

    @DaoFactory
    OwnerReactiveDao ownerDao(@DaoKeyspace CqlIdentifier keyspace);
     
}
