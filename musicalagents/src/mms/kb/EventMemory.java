package mms.kb;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mms.Parameters;
import mms.clock.TimeUnit;

public class EventMemory extends Memory {

	/**
	 *  Lock
	 */
	private Lock lock = new ReentrantLock();
	
	EventSlot head = null;
	EventSlot tail = null;

	public int size = 0;
	
	EventSlot ptr_last_instant_read = null;

	@Override
	public void init(Parameters parameters) {
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

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
	 * Cleans memory beyond time horizon (past and future parameters)
	 */
	public void updateMemory() {
		
		double horizon = clock.getCurrentTime(TimeUnit.SECONDS) - past;
		EventSlot ptr = head;
		while (ptr != null) {
			if (ptr.instant >= horizon) {
				head = ptr;
				break;
			}
			ptr = ptr.next;
			size--;
		}
		if (ptr == null) {
			ptr_last_instant_read = null;
			head = null;
			tail = null;
		} else if (ptr.instant > ptr_last_instant_read.instant) {
			ptr_last_instant_read = head;
		}
		
	}
	
	@Override
	public Object readMemory(double instant, TimeUnit unit) {
		
		if (head == null) {
			
			System.err.println("[ERROR] Memory has no state!");
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
