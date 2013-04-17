/******************************************************************************

Copyright 2011 Leandro Ferrari Thomaz

This file is part of Ensemble.

Ensemble is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ensemble is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble.world;

import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * Represents the state of an entity in the environment (agent or not) at a given instant. 
 * @author lfthomaz
 *
 */
public class EntityState {

	// State instant in seconds
	/** The instant. */
	public double instant;
	
	/** The attributes. */
	protected HashMap<String,Object> attributes;
	
	/**
	 * Instantiates a new entity state.
	 */
	public EntityState() {
		attributes = new HashMap<String, Object>();
	}
	
	/**
	 * Gets the instant.
	 *
	 * @return the instant
	 */
	public double getInstant() {
		return this.instant;
	}
	
	/**
	 * Gets the entity state attribute.
	 *
	 * @param attributeName the attribute name
	 * @return the entity state attribute
	 */
	public Object getEntityStateAttribute(String attributeName) {
		return attributes.get(attributeName);
	}
		
    /**
     * Updates a variable from an entity's state. This method can be overloaded by the user to check the data beign updated.
     *
     * @param entityName the entity name
     * @param variable the variable
     * @param value the value
     * @return if the update was successful
     */
    public boolean updateEntityStateAttribute(String entityName, String variable, Object value) {
    	// Aqui podemos fazer uma checagem no valor variável, para ver se é válido
    	boolean result = true;
    	attributes.put(variable, value);
    	return result;
    }
    
}
