/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xtract.core;

public final class xtract_feature_init_ {
  public final static xtract_feature_init_ XTRACT_INIT_MFCC = new xtract_feature_init_("XTRACT_INIT_MFCC", xtractJNI.XTRACT_INIT_MFCC_get());
  public final static xtract_feature_init_ XTRACT_INIT_BARK = new xtract_feature_init_("XTRACT_INIT_BARK");
  public final static xtract_feature_init_ XTRACT_INIT_WINDOWED = new xtract_feature_init_("XTRACT_INIT_WINDOWED");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static xtract_feature_init_ swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + xtract_feature_init_.class + " with value " + swigValue);
  }

  private xtract_feature_init_(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private xtract_feature_init_(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private xtract_feature_init_(String swigName, xtract_feature_init_ swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static xtract_feature_init_[] swigValues = { XTRACT_INIT_MFCC, XTRACT_INIT_BARK, XTRACT_INIT_WINDOWED };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
