package com.neo.service.combinatorial;

import java.util.ArrayList;

/**
 * Basic data structure and evaluations for classic test suite
 */
public class TestSuite {

  public ArrayList<TestCase> suite;

  public TestSuite() {
    this.suite = new ArrayList<>();
  }

  public int size() {
    return suite.size() ;
  }

  public void show() {
    for (int i = 0; i < suite.size(); i++) {
      for (int j = 0; j < suite.get(i).test.length; j++)
        System.out.print(suite.get(i).test[j] + " ");
      System.out.println();
    }
  }

}
