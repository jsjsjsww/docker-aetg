package com.neo.service.generator;

import com.neo.service.combinatorial.CTModel;
import com.neo.service.combinatorial.TestSuite;

public interface CAGenerator {

  /**
   * The method to construct a t-way constrained covering
   * array for a given test IPOModel.
   * @param model a combinatorial test IPOModel
   * @param ts    the generated test suite
   */
  void generation(CTModel model, TestSuite ts);
}
