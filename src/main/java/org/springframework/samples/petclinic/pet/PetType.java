package org.springframework.samples.petclinic.pet;

import java.io.Serializable;

// TODO: return value for what? operations on REST API?
/**
 * Return Value.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class PetType implements Serializable {
    
    private static final long serialVersionUID = -6848331396030706076L;

    private String id;
    
    private String name;
    
    public PetType() {}
    
    public PetType(String name) {
        this.id   = name;
        this.name = name;
    }

    /**
     * Getter accessor for attribute 'id'.
     *
     * @return
     *       current value of 'id'
     */
    public String getId() {
        return id;
    }

    /**
     * Setter accessor for attribute 'id'.
     * @param id
     * 		new value for 'id '
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter accessor for attribute 'name'.
     *
     * @return
     *       current value of 'name'
     */
    public String getName() {
        return name;
    }

    /**
     * Setter accessor for attribute 'name'.
     * @param name
     * 		new value for 'name '
     */
    public void setName(String name) {
        this.name = name;
    }
    
    

}
