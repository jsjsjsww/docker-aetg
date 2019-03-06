package com.neo.service.handler;

import java.util.Arrays;

public class Constraint implements Cloneable {

  /*
   *  To use the sat4j solver, each parameter value must be mapped
   *  into an integer value, which starts from 1.
   *
   *  For example, the mapping for CA(N;t,5,3) is as follows:
   *  p1  p2  p3  p4  p5
   *   1   4   7  10  13
   *   2   5   8  11  14
   *   3   6   9  12  15
   *
   *  A constraint is represented as a disjunction of literals.
   *  For example, the followings give the representations of
   *  two forbidden combinations.
   *  {0,  -1, 0, -1, -1} as [-1, -7]
   *  {-1, -1, 2,  0,  1} as [-9, -10, -14]
   */

  // constraint representation
  public int[] disjunction;

  /**
   * Set constraint according to a disjunction of literals
   * @param clause disjunction of literals
   */
  public Constraint(final int[] clause) {
    disjunction = new int[clause.length];
    System.arraycopy(clause, 0, disjunction, 0, clause.length);
  }

  @Override
  public Constraint clone() {
    final Constraint cs ;
    try {
      cs = (Constraint)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
    cs.disjunction = this.disjunction.clone();
    return cs;
  }

  @Override
  public String toString() {
    return Arrays.toString(disjunction);
  }

  public boolean isSuperior(Constraint con){
    if(con.disjunction.length < this.disjunction.length)
      return false;
    for(int i=0; i<this.disjunction.length;i++) {
      int j;
      for (j = 0; j < con.disjunction.length; j++) {
        if (con.disjunction[j] == this.disjunction[i])
          break;
      }
      if(j==con.disjunction.length && this.disjunction[i]!=con.disjunction[j-1])
        return false;
    }
    return true;

  }
}
