package jjack.test;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import jjack.JackCallback;
import jjack.JackPortFlags;
import jjack.SWIGTYPE_p_jack_client_t;
import jjack.SWIGTYPE_p_jack_port_t;
import jjack.jjack;
import jjack.jjackConstants;


public class Test {

	private static long client;
	private static long port;

	public static void main(String[] args) {

		client = jjack.jack_client_open("jjack", new JackCallback() {
	        double t = 0;
	        double freq = 440.0;
	        double fs = 44100.0;
	        double step = 1/fs;
			@Override
			public int process(int nframes, double time) {
//                System.out.printf("Java::callback(%d)\n", nframes);
				//List the ports 
				
				/*String[] playback_ports = jjack.jack_get_ports(client, null, null,JackPortFlags.JackPortIsOutput);
				if (playback_ports == null) {
					System.err.println("Cannot find any physical playback ports");
					System.exit(1);
				}
				for (int i = 0; i < playback_ports.length; i++) {
					System.out.println(playback_ports[i]);
				}
*/
			
				
				FloatBuffer fOut = jjack.jack_port_get_buffer(port, nframes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
                while (fOut.remaining() > 0) {
                        double dSample = 0.1 * Math.sin(2 * Math.PI * freq * t);
                        fOut.put((float)dSample);
                        t = t + step;
                }
                return 0;
			}
		});

		if (client == 0) {
			System.err.println("Error");
			System.exit(1);
		}

		/* Capture ports */
		System.out.println("JACK Capture ports");
		String[] cap_ports = jjack.jack_get_ports(client, null, jjack.JACK_DEFAULT_AUDIO_TYPE, JackPortFlags.JackPortIsOutput);
		for (int i = 0; i < cap_ports.length; i++) {
			System.out.println("\t"+cap_ports[i]);
		}
		
		/* Playback ports */
		System.out.println("JACK Playback ports");
		String[] pla_ports = jjack.jack_get_ports(client, null, jjack.JACK_DEFAULT_AUDIO_TYPE, JackPortFlags.JackPortIsInput);
		for (int i = 0; i < pla_ports.length; i++) {
			System.out.println("\t"+pla_ports[i]);
		}

		port = jjack.jack_port_register(client, 
										"port_1",
										jjackConstants.JACK_DEFAULT_AUDIO_TYPE, 
										JackPortFlags.JackPortIsOutput);

		// Activates the JACK client
		if (jjack.jack_activate(client) != 0) {
			System.err.println("Error");
		}

		jjack.jack_connect(client, "jjack:port_1", "system:playback_1");
		
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
}
