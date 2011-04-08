/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package mmsportaudio;

public class PaStreamInfo {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected PaStreamInfo(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(PaStreamInfo obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      portaudioJNI.delete_PaStreamInfo(swigCPtr);
    }
    swigCPtr = 0;
  }

  public void setStructVersion(int value) {
    portaudioJNI.PaStreamInfo_structVersion_set(swigCPtr, this, value);
  }

  public int getStructVersion() {
    return portaudioJNI.PaStreamInfo_structVersion_get(swigCPtr, this);
  }

  public void setInputLatency(double value) {
    portaudioJNI.PaStreamInfo_inputLatency_set(swigCPtr, this, value);
  }

  public double getInputLatency() {
    return portaudioJNI.PaStreamInfo_inputLatency_get(swigCPtr, this);
  }

  public void setOutputLatency(double value) {
    portaudioJNI.PaStreamInfo_outputLatency_set(swigCPtr, this, value);
  }

  public double getOutputLatency() {
    return portaudioJNI.PaStreamInfo_outputLatency_get(swigCPtr, this);
  }

  public void setSampleRate(double value) {
    portaudioJNI.PaStreamInfo_sampleRate_set(swigCPtr, this, value);
  }

  public double getSampleRate() {
    return portaudioJNI.PaStreamInfo_sampleRate_get(swigCPtr, this);
  }

  public PaStreamInfo() {
    this(portaudioJNI.new_PaStreamInfo(), true);
  }

}
