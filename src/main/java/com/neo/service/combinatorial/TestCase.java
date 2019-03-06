package com.neo.service.combinatorial;

import java.util.Arrays;

public class TestCase {
  public final int[] test;

  public TestCase(int[] t) {
    test = t.clone();
  }

  @Override
  public boolean equals(Object other){
    if (other == null || !(other instanceof TestCase))
      return false;
    if (other == this)
      return true;

    TestCase tc = (TestCase) other;
    return Arrays.equals(this.test, tc.test);
  }

  @Override
  public String toString() {
    return Arrays.toString(test);
  }

}
