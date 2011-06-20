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

package jjack;

import java.nio.ByteBuffer;

public class jjack implements jjackConstants {

	// Loads the portaudio JNI interface
	static {
		try {
			if (System.getProperty("os.name").startsWith("Windows")) {
				if (System.getProperty("sun.arch.data.model").equals("32")) {
					System.loadLibrary("jjack");
				} else {
					System.loadLibrary("jjack");
				}
			}
			else if (System.getProperty("os.name").startsWith("Mac OS X")) {
				System.loadLibrary("jjack");
			}
			else if (System.getProperty("os.name").startsWith("Unix")) {
				System.loadLibrary("jjack");
			}		
		} catch (UnsatisfiedLinkError  e) {
			e.printStackTrace();
			System.err.println("jjack library not found... JACK will not be available!");
		}
	}

	
  public static int jack_client_close(long client) {
    return jjackJNI.jack_client_close(client);
  }

  public static long jack_client_open(String client_name, Object callback) {
    return jjackJNI.jack_client_open(client_name, callback);
  }

  public static int jack_get_sample_rate(long arg0) {
    return jjackJNI.jack_get_sample_rate(arg0);
  }

  public static long jack_port_register(long client, String port_name, String port_type, long flags) {
    return jjackJNI.jack_port_register(client, port_name, port_type, flags);
  }

  public static int jack_port_unregister(long arg0, long arg1) {
	    return jjackJNI.jack_port_unregister(arg0, arg1);
  }

  public static int jack_activate(long client) {
    return jjackJNI.jack_activate(client);
  }

  public static String[] jack_get_ports(long arg0, String port_name_pattern, String type_name_pattern, long flags) {
    return jjackJNI.jack_get_ports(arg0, port_name_pattern, type_name_pattern, flags);
}

  public static int jack_connect(long arg0, String source_port, String destination_port) {
    return jjackJNI.jack_connect(arg0, source_port, destination_port);
  }

  public static int jack_disconnect(long arg0, String source_port, String destination_port) {
	    return jjackJNI.jack_disconnect(arg0, source_port, destination_port);
	  }

  public static String jack_port_name(long port) {
    return jjackJNI.jack_port_name(port);
  }

  public static ByteBuffer jack_port_get_buffer(long arg0, int arg1) {
	    return jjackJNI.jack_port_get_buffer(arg0, arg1);
  }

//  public static int jack_set_process_callback(long client, Object process_callback) {
//    return jjackJNI.jack_set_process_callback(client), process_callback);
//  }

  public static long jack_port_by_name(long arg0, String port_name) {
    return jjackJNI.jack_port_by_name(arg0, port_name);
  }

  public static int jack_port_get_latency(long port) {
    return jjackJNI.jack_port_get_latency(port);
  }

  public static int jack_port_get_total_latency(long arg0, long port) {
    return jjackJNI.jack_port_get_total_latency(arg0, port);
  }

  public static int jack_frames_since_cycle_start(long arg0) {
    return jjackJNI.jack_frames_since_cycle_start(arg0);
  }

  public static int jack_frame_time(long arg0) {
    return jjackJNI.jack_frame_time(arg0);
  }

  public static int jack_last_frame_time(long client) {
    return jjackJNI.jack_last_frame_time(client);
  }

  public static long jack_frames_to_time(long client, int arg1) {
    return jjackJNI.jack_frames_to_time(client, arg1);
  }

  public static int jack_time_to_frames(long client, long arg1) {
    return jjackJNI.jack_time_to_frames(client, arg1);
  }

  public static long jack_get_time() {
    return jjackJNI.jack_get_time();
  }
}
