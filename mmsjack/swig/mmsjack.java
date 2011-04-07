/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package mmsjack;

public class mmsjack implements mmsjackConstants {
  public static Object test() {
    return mmsjackJNI.test();
  }

  public static SWIGTYPE_p_jack_client_t jack_client_new(String client_name) {
    long cPtr = mmsjackJNI.jack_client_new(client_name);
    return (cPtr == 0) ? null : new SWIGTYPE_p_jack_client_t(cPtr, false);
  }

  public static int jack_client_close(SWIGTYPE_p_jack_client_t client) {
    return mmsjackJNI.jack_client_close(SWIGTYPE_p_jack_client_t.getCPtr(client));
  }

  public static SWIGTYPE_p_jack_client_t jack_client_open(String client_name, SWIGTYPE_p_jack_options_t options, SWIGTYPE_p_jack_status_t status) {
    long cPtr = mmsjackJNI.jack_client_open(client_name, SWIGTYPE_p_jack_options_t.getCPtr(options), SWIGTYPE_p_jack_status_t.getCPtr(status));
    return (cPtr == 0) ? null : new SWIGTYPE_p_jack_client_t(cPtr, false);
  }

  public static int jack_get_sample_rate(SWIGTYPE_p_jack_client_t arg0) {
    return mmsjackJNI.jack_get_sample_rate(SWIGTYPE_p_jack_client_t.getCPtr(arg0));
  }

  public static SWIGTYPE_p_jack_port_t jack_port_register(SWIGTYPE_p_jack_client_t client, String port_name, String port_type, long flags, long buffer_size) {
    long cPtr = mmsjackJNI.jack_port_register(SWIGTYPE_p_jack_client_t.getCPtr(client), port_name, port_type, flags, buffer_size);
    return (cPtr == 0) ? null : new SWIGTYPE_p_jack_port_t(cPtr, false);
  }

  public static int jack_activate(SWIGTYPE_p_jack_client_t client) {
    return mmsjackJNI.jack_activate(SWIGTYPE_p_jack_client_t.getCPtr(client));
  }

  public static String[] jack_get_ports(SWIGTYPE_p_jack_client_t arg0, String port_name_pattern, String type_name_pattern, long flags) {
    return mmsjackJNI.jack_get_ports(SWIGTYPE_p_jack_client_t.getCPtr(arg0), port_name_pattern, type_name_pattern, flags);
}

  public static int jack_connect(SWIGTYPE_p_jack_client_t arg0, String source_port, String destination_port) {
    return mmsjackJNI.jack_connect(SWIGTYPE_p_jack_client_t.getCPtr(arg0), source_port, destination_port);
  }

  public static int jack_disconnect(SWIGTYPE_p_jack_client_t arg0, String source_port, String destination_port) {
    return mmsjackJNI.jack_disconnect(SWIGTYPE_p_jack_client_t.getCPtr(arg0), source_port, destination_port);
  }

  public static String jack_port_name(SWIGTYPE_p_jack_port_t port) {
    return mmsjackJNI.jack_port_name(SWIGTYPE_p_jack_port_t.getCPtr(port));
  }

  public static SWIGTYPE_p_void jack_port_get_buffer(SWIGTYPE_p_jack_port_t arg0, int arg1) {
    long cPtr = mmsjackJNI.jack_port_get_buffer(SWIGTYPE_p_jack_port_t.getCPtr(arg0), arg1);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static int jack_set_process_callback(SWIGTYPE_p_jack_client_t client, SWIGTYPE_p_JackProcessCallback process_callback, SWIGTYPE_p_void arg) {
    return mmsjackJNI.jack_set_process_callback(SWIGTYPE_p_jack_client_t.getCPtr(client), SWIGTYPE_p_JackProcessCallback.getCPtr(process_callback), SWIGTYPE_p_void.getCPtr(arg));
  }

  public static SWIGTYPE_p_jack_port_t jack_port_by_name(SWIGTYPE_p_jack_client_t arg0, String port_name) {
    long cPtr = mmsjackJNI.jack_port_by_name(SWIGTYPE_p_jack_client_t.getCPtr(arg0), port_name);
    return (cPtr == 0) ? null : new SWIGTYPE_p_jack_port_t(cPtr, false);
  }

  public static int jack_port_get_latency(SWIGTYPE_p_jack_port_t port) {
    return mmsjackJNI.jack_port_get_latency(SWIGTYPE_p_jack_port_t.getCPtr(port));
  }

  public static int jack_port_get_total_latency(SWIGTYPE_p_jack_client_t arg0, SWIGTYPE_p_jack_port_t port) {
    return mmsjackJNI.jack_port_get_total_latency(SWIGTYPE_p_jack_client_t.getCPtr(arg0), SWIGTYPE_p_jack_port_t.getCPtr(port));
  }

  public static int jack_frames_since_cycle_start(SWIGTYPE_p_jack_client_t arg0) {
    return mmsjackJNI.jack_frames_since_cycle_start(SWIGTYPE_p_jack_client_t.getCPtr(arg0));
  }

  public static int jack_frame_time(SWIGTYPE_p_jack_client_t arg0) {
    return mmsjackJNI.jack_frame_time(SWIGTYPE_p_jack_client_t.getCPtr(arg0));
  }

  public static int jack_last_frame_time(SWIGTYPE_p_jack_client_t client) {
    return mmsjackJNI.jack_last_frame_time(SWIGTYPE_p_jack_client_t.getCPtr(client));
  }

  public static SWIGTYPE_p_jack_time_t jack_frames_to_time(SWIGTYPE_p_jack_client_t client, int arg1) {
    return new SWIGTYPE_p_jack_time_t(mmsjackJNI.jack_frames_to_time(SWIGTYPE_p_jack_client_t.getCPtr(client), arg1), true);
  }

  public static int jack_time_to_frames(SWIGTYPE_p_jack_client_t client, SWIGTYPE_p_jack_time_t arg1) {
    return mmsjackJNI.jack_time_to_frames(SWIGTYPE_p_jack_client_t.getCPtr(client), SWIGTYPE_p_jack_time_t.getCPtr(arg1));
  }

  public static SWIGTYPE_p_jack_time_t jack_get_time() {
    return new SWIGTYPE_p_jack_time_t(mmsjackJNI.jack_get_time(), true);
  }

  public static int jack_port_unregister(SWIGTYPE_p_jack_client_t arg0, SWIGTYPE_p_jack_port_t arg1) {
    return mmsjackJNI.jack_port_unregister(SWIGTYPE_p_jack_client_t.getCPtr(arg0), SWIGTYPE_p_jack_port_t.getCPtr(arg1));
  }

}