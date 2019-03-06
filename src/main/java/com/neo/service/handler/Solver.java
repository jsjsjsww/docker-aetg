package com.neo.service.handler;

import com.neo.service.common.ALG;
import com.neo.service.combinatorial.CTModel;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Use the sat4j solver (http://www.sat4j.org) as the validity checker.
 * Current version uses a basic SAT (boolean) encoding to model constraints.
 */
public class Solver implements ValidityChecker {

  private int[][] relation;
  private Vector<Constraint> basicConstraint;  // at-least & at-most constraints
  private Vector<Constraint> hardConstraint;   // user specified constraints

  private ISolver solver;  // the SAT solver

  public Solver() {
    basicConstraint = new Vector<>();
    hardConstraint = new Vector<>();
  }

  /**
   * Initialize a validity checker.
   * @param model an object of CT model
   */
  public void init(CTModel model) {
    basicConstraint = new Vector<>();
    hardConstraint = new Vector<>();
    relation = model.relation;

    // set at-least constraint
    for (int i = 0; i < model.parameter; i++) {
      basicConstraint.add(new Constraint(relation[i]));
    }

    // set at-most constraint
    for (int i = 0; i < model.parameter; i++) {
      for (int[] row : ALG.allCombination(model.value[i], 2)) {
        int[] tp = {0 - relation[i][row[0]], 0 - relation[i][row[1]]};
        basicConstraint.add(new Constraint(tp));
      }
    }

    // set hard constraints
    if(model.constraint!=null)
    for (int[] x : model.constraint) {
      hardConstraint.add(new Constraint(x));
    }

    // initialize solver
    int MAXVAR = relation[model.parameter-1][model.value[model.parameter-1]-1];
    int NBCLAUSES = basicConstraint.size() + hardConstraint.size();
    solver = SolverFactory.newDefault();
    solver.newVar(MAXVAR);
    solver.setExpectedNumberOfClauses(NBCLAUSES);

    try {
      for (Constraint clause : basicConstraint)
        solver.addClause(new VecInt(clause.disjunction));
      for (Constraint clause : hardConstraint)
        solver.addClause(new VecInt(clause.disjunction));
    } catch (ContradictionException e) {
      System.err.println("Solver Contradiction Error: " + e.getMessage());
    }
  }

  /**
   * Determine whether a given complete or partial test case is constraints satisfying.
   * Any unfixed parameters are assigned to value -1.
   * @param test a complete or partial test case
   */
  public boolean isValid(final int[] test) {
    if (hardConstraint.size() == 0)
      return true;

    // transfer test to clause representation
    ArrayList<Integer> list = new ArrayList<>();
    for (int i = 0; i < test.length; i++) {
      if (test[i] != -1)
        list.add(relation[i][test[i]]);
    }
    int[] clause = list.stream().mapToInt(i -> i).toArray();

    // determine validity
    boolean satisfiable = false;
    try {
      VecInt c = new VecInt(clause);
      IProblem problem = solver;
      satisfiable = problem.isSatisfiable(c);
    } catch (TimeoutException e) {
      System.err.println("Solver Timeout Error: " + e.getMessage());
    }
    return satisfiable;
  }

}
