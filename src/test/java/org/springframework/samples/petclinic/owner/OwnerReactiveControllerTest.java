package org.springframework.samples.petclinic.owner;

import static org.mockito.ArgumentMatchers.any;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.conf.MappingUtils;
import org.springframework.samples.petclinic.owner.db.OwnerEntity;
import org.springframework.samples.petclinic.owner.db.OwnerReactiveDao;
import org.springframework.samples.petclinic.owner.db.OwnerReactiveServicesImpl;
import org.springframework.samples.petclinic.pet.db.PetReactiveDao;
import org.springframework.samples.petclinic.visit.db.VisitReactiveDao;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.BodyInserters;

import reactor.core.publisher.Mono;

/**
 * Testing the {@link OwnerReactiveController} layer.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
//@ExtendWith(SpringExtension.class)
//@WebFluxTest(controllers = OwnerReactiveController.class)
@Import(OwnerReactiveServicesImpl.class)
public class OwnerReactiveControllerTest {
    
    @MockBean
    PetReactiveDao petDao;
    
    @MockBean
    VisitReactiveDao visitDao;

    @MockBean
    OwnerReactiveDao ownerDao;
 
    @Autowired
    private WebTestClient webClient;
 
    //@Test
    void testCreateOwner() throws UnsupportedEncodingException {
        Owner o1 = new Owner(
                UUID.fromString("11111111-1111-1111-1111-111111111111"), "John", 
                                "Connor", "T800 street", "Detroit", "0123456789");
        OwnerEntity dto = MappingUtils.mapOwnerAsEntity(o1);
        Mockito.when(ownerDao.save(any())).thenReturn(Mono.just(dto));
        webClient.post()
                 .uri("/petclinic/api/owners")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("Authorization", "Basic " + Base64Utils
                         .encodeToString(("user:user").getBytes("UTF-8")))
                 .body(BodyInserters.fromValue(o1))
                 .exchange()
                 .expectStatus().isCreated()
                 .expectBody(Owner.class);
        
        // KO as the UID is generated at controller side, not same object
        // Mockito.verify(ownerDao, times(1)).save(dto);
        // <--
    }
    

}
