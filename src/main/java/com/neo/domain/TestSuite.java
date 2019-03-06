package com.neo.domain;

import java.util.ArrayList;

public class TestSuite {
  private ArrayList<int[]> testsuite;
  private long time;

  public TestSuite(ArrayList<int[]> testsuite, long time){
    this.testsuite = testsuite;
    this.time = time;
  }

  public double getTime() {
	return time;
  }

  public ArrayList<int[]> getTestsuite() {
	return testsuite;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
