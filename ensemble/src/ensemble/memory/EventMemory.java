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

package ensemble.memory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ensemble.Parameters;
import ensemble.clock.TimeUnit;


public class EventMemory extends Memory {

	private Lock lock = new ReentrantLock();
	
	EventSlot head = null;
	EventSlot tail = null;

	public int size = 0;
	
	EventSlot ptr_last_instant_read = null;

	@Override
	public double getFirstInstant() {
		if (head != null) {
			return head.instant;
		} else {
			return Double.MAX_VALUE;
		}
	}

	@Override
	public double getLastInstant() {
		if (tail != null) {
			return tail.instant;
		} else {
			return Double.MIN_VALUE;
		}
	}

	/**
	 * Cleans memory beyond time horizon (past and future parameters).
	 * It always leaves the most recent event in memory, so it will never be empty.
	 */
	public void updateMemory() {
		
		double horizon = clock.getCurrentTime(TimeUnit.SECONDS) - past;
		EventSlot ptr = head;
		if (ptr != null) {
			while (ptr.next != null) {
				if (ptr.instant >= horizon) {
					head = ptr;
					break;
				}
				ptr = ptr.next;
				size--;
			}
			if (ptr_last_instant_read != null && ptr.instant > ptr_last_instant_read.instant) {
				ptr_last_instant_read = head;
			}
		}
		
	}
	
	@Override
	public Object readMemory(double instant, TimeUnit unit) {
		
		if (head == null) {
			
//			System.out.println("Memory is empty!");
			return null;

		} else {

			// Heuristics -> trying the last searched instant
			EventSlot ptr;
			if (ptr_last_instant_read == null) {
				ptr = head;
				while (ptr.next != null) {
					if (ptr.next.instant > instant) {
						break;
					}
					ptr = ptr.next;
				}
			} else {
				ptr = ptr_last_instant_read;
				if (instant >= ptr.instant) {
					while (ptr.next != null) {
						if (ptr.next.instant > instant) {
							break;
						}
						ptr = ptr.next;
					}
				} else {
					while (ptr.prev != null) {
						if (instant >= ptr.prev.instant) {
							ptr = ptr.prev;
							break;
						}
						ptr = ptr.prev;
					}
				}
			}
			
			ptr_last_instant_read = ptr;
				
			return ptr.object;

		}
		
	}

	@Override
	public Object readMemory(double instant, double duration, TimeUnit unit) {
		
		return readMemory(instant, unit);
		
	}

	@Override
	public void resetMemory() {
		head = null;
		size = 0;
	}

	@Override
	public void writeMemory(Object object, double instant, double duration,
			TimeUnit unit) throws MemoryException {

//		System.out.println("writeMemory = " + instant);
		
		updateMemory();

		// TODO Garantir que a unidade (unit) é a mesma
		
		// Creates a new slot to store the object
		EventSlot evt = new EventSlot();
		evt.instant = instant;
		evt.object = object;
		
		// Searches the right place to insert the EventSlot
		// Heuristics - begins from the tail
		if (tail == null) {
			head = evt;
			tail = evt;
		} else {
			EventSlot ptr = tail;
			EventSlot ptr_next = tail.next;
			while (ptr != null) {
				if (instant > ptr.instant) {
					if (ptr_next != null) {
						evt.next = ptr_next;
						ptr_next.prev = evt;
					} else {
						// New tail
						tail = evt;
					}
					ptr.next = evt;
					evt.prev = ptr;
					break;
				}
				ptr_next = ptr;
				ptr = ptr.prev;
			}
			// New head
			if (ptr == null) {
				head.prev = evt;
				evt.next = head;
				head = evt;
			}
			
		}
		
		size++;
		
	}
	
	@Override
	public void writeMemory(Object object, double instant, TimeUnit unit) throws MemoryException {

		writeMemory(object, instant, 0, TimeUnit.SECONDS);
	
	}
	
	@Override
	public void writeMemory(Object object) throws MemoryException {
		
		double instant = clock.getCurrentTime(TimeUnit.SECONDS);
		writeMemory(object, instant, 0, TimeUnit.SECONDS);
		
	}

	class EventSlot {
		public double 		instant;
		public Object 		object;
		public EventSlot 	prev;
		public EventSlot 	next;
	}
	
}
