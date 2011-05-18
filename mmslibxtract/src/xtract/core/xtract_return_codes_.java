/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xtract.core;

public final class xtract_return_codes_ {
  public final static xtract_return_codes_ XTRACT_SUCCESS = new xtract_return_codes_("XTRACT_SUCCESS");
  public final static xtract_return_codes_ XTRACT_MALLOC_FAILED = new xtract_return_codes_("XTRACT_MALLOC_FAILED");
  public final static xtract_return_codes_ XTRACT_BAD_ARGV = new xtract_return_codes_("XTRACT_BAD_ARGV");
  public final static xtract_return_codes_ XTRACT_BAD_VECTOR_SIZE = new xtract_return_codes_("XTRACT_BAD_VECTOR_SIZE");
  public final static xtract_return_codes_ XTRACT_DENORMAL_FOUND = new xtract_return_codes_("XTRACT_DENORMAL_FOUND");
  public final static xtract_return_codes_ XTRACT_NO_RESULT = new xtract_return_codes_("XTRACT_NO_RESULT");
  public final static xtract_return_codes_ XTRACT_FEATURE_NOT_IMPLEMENTED = new xtract_return_codes_("XTRACT_FEATURE_NOT_IMPLEMENTED");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static xtract_return_codes_ swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + xtract_return_codes_.class + " with value " + swigValue);
  }

  private xtract_return_codes_(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private xtract_return_codes_(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private xtract_return_codes_(String swigName, xtract_return_codes_ swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static xtract_return_codes_[] swigValues = { XTRACT_SUCCESS, XTRACT_MALLOC_FAILED, XTRACT_BAD_ARGV, XTRACT_BAD_VECTOR_SIZE, XTRACT_DENORMAL_FOUND, XTRACT_NO_RESULT, XTRACT_FEATURE_NOT_IMPLEMENTED };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
