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

package ensemble;

import java.io.Serializable;

import ensemble.clock.TimeUnit;


// TODO: Auto-generated Javadoc
/**
 * The Class Event.
 */
public class Event implements Serializable {

	// Identificação do Evento
	// qual agente gerou, qual o componente e qual o tipo de evento
	/** The dest agent name. */
	public String	destAgentName;
	
	/** The dest agent comp name. */
	public String	destAgentCompName;
	
	/** The ori agent name. */
	public String	oriAgentName;
	
	/** The ori agent comp name. */
	public String	oriAgentCompName;
	
	/** The event type. */
	public String	eventType;

	// Timestamp do evento. No caso de um evento periódico, corresponde ao instante inicial do frame, caso contrário, é o instante em que foi enviado
	// TODO não necessariamente precisa ser o tempo em ms, pode ser o turno
	/** The timestamp. */
	public long 	timestamp;
	
	// No caso de eventos periódicos, indica a qual frame esse evento pertence
	/** The frame. */
	public long 	frame;
	
	// Instante e duração no tempo (em s) em que o evento acontece
	// No caso de eventos periódicos, corresponde ao ínico do frame, caso contrário, ao momento em que foi enviado
	/** The instant. */
	public double 	instant;
	
	/** The duration. */
	public double 	duration;
	
	/** The unit. */
	public TimeUnit unit;
	
	// Conteúdo do evento (caso necessite de algo mais complexo, estender a classe)
	/** The obj content. */
	public Object 	objContent;
	
	/**
	 * Instantiates a new event.
	 */
	public Event() {
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str;
		str = oriAgentName + ":" + oriAgentCompName + " -> " + destAgentName + ":" + destAgentCompName + " / frame = " + frame + " instant = " + instant + " duration = " + duration;
		return str;
	}
	
	// TODO Transforma um evento em uma mensagem ACL
	/**
	 * To acl message.
	 *
	 * @return the string
	 */
	public String toACLMessage() {
		return null;
	}
	
}
