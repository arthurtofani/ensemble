/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xtract.core;

public class xtract_function_descriptor_t_result {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected xtract_function_descriptor_t_result(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(xtract_function_descriptor_t_result obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      xtractJNI.delete_xtract_function_descriptor_t_result(swigCPtr);
    }
    swigCPtr = 0;
  }

  public xtract_function_descriptor_t_result_vector getVector() {
    long cPtr = xtractJNI.xtract_function_descriptor_t_result_vector_get(swigCPtr, this);
    return (cPtr == 0) ? null : new xtract_function_descriptor_t_result_vector(cPtr, false);
  }

  public xtract_function_descriptor_t_result_scalar getScalar() {
    long cPtr = xtractJNI.xtract_function_descriptor_t_result_scalar_get(swigCPtr, this);
    return (cPtr == 0) ? null : new xtract_function_descriptor_t_result_scalar(cPtr, false);
  }

  public xtract_function_descriptor_t_result() {
    this(xtractJNI.new_xtract_function_descriptor_t_result(), true);
  }

}
