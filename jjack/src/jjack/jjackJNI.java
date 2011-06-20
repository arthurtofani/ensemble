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

class jjackJNI {
  public final static native String JACK_DEFAULT_AUDIO_TYPE_get();
  public final static native String JACK_DEFAULT_MIDI_TYPE_get();
  public final static native int JackPortIsInput_get();
  public final static native int JackPortIsOutput_get();
  public final static native int JackPortIsPhysical_get();
  public final static native int JackPortCanMonitor_get();
  public final static native int JackPortIsTerminal_get();
//  public final static native int JackPortIsActive_get();
  public final static native int JackNullOption_get();
  public final static native int JackNoStartServer_get();
  public final static native int JackUseExactName_get();
  public final static native int JackServerName_get();
  public final static native int JackLoadName_get();
  public final static native int JackLoadInit_get();
  public final static native int jack_client_close(long jarg1);
  public final static native long jack_client_open(String jarg1, Object jarg2);
  public final static native int jack_get_sample_rate(long jarg1);
  public final static native long jack_port_register(long jarg1, String jarg2, String jarg3, long jarg4);
  public final static native int jack_port_unregister(long jarg1, long jarg2);
  public final static native int jack_activate(long jarg1);
  public final static native String[] jack_get_ports(long jarg1, String jarg2, String jarg3, long jarg4);
  public final static native int jack_connect(long jarg1, String jarg2, String jarg3);
  public final static native int jack_disconnect(long jarg1, String jarg2, String jarg3);
  public final static native String jack_port_name(long jarg1);
  public final static native ByteBuffer jack_port_get_buffer(long jarg1, int jarg2);
//  public final static native int jack_set_process_callback(long jarg1, Object jarg2);
  public final static native long jack_port_by_name(long jarg1, String jarg2);
  public final static native int jack_port_get_latency(long jarg1);
  public final static native int jack_port_get_total_latency(long jarg1, long jarg2);
  public final static native int jack_frames_since_cycle_start(long jarg1);
  public final static native int jack_frame_time(long jarg1);
  public final static native int jack_last_frame_time(long jarg1);
  public final static native long jack_frames_to_time(long jarg1, int jarg2);
  public final static native int jack_time_to_frames(long jarg1, long jarg2);
  public final static native long jack_get_time();
}
