package com.neo.service.generator;



import com.neo.service.combinatorial.*;
import com.neo.service.common.*;
import com.neo.service.handler.MFTVerifier;


import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


public class AETG implements CAGenerator {

  /**
   * index-number pair: particularly, index represents a candidate parameter
   * or value, and number represents the number of uncovered combinations
   * that are related to this parameter or value
   */
  private class Pair implements Comparable<Pair> {
	public int index;
	public int number;

	private Pair(int i, int n) {
	  index = i;
	  number = n;
	}

	/*
	 *  compareTo should return < 0 if this is supposed to be
	 *  less than other, > 0 if this is supposed to be greater than
	 *  other and 0 if they are supposed to be equal
	 *
	 *  do a descending solution via collection.sort (collection.sort is
	 *  ascending), and sorting is only based on the value of number
	 */
	@Override
	public int compareTo(Pair B) {
	  return -Integer.compare(this.number, B.number);
	}

	@Override
	public String toString() {
	  return String.valueOf(index) + " (" + String.valueOf(number) + ")";
	}
  }

  public CTModel model;
  private int CANDIDATE = 50;
  private Random random;
  private boolean debug = false;

  public AETG() {
	model = null;
	random = new Random();
  }

  public void setDebug(boolean debug) {
	this.debug = debug;
  }

  public void setCANDIDATE(int candidate) {
	this.CANDIDATE = candidate;
  }

  /**
   * The main AETG repair framework
   */
  public void generation(CTModel model, TestSuite ts) {
	this.model = model;
	this.model.initialization();
	ts.suite.clear();

	while (model.getCombUncovered() != 0) {
	  int[] next = nextBestTestCase(CANDIDATE);
	  // no more combinations that need to cover
	  if (next == null)
		break;

	  TestCase best = new TestCase(next);
	  ts.suite.add(best);
	  model.updateCombination(best.test);

	  if (debug)
		System.out.println(String.format("[AETG] row %d, uncovered %d", ts.suite.size(), model.getCombUncovered()));
	}
  }

  /**
   * Return the next test case that covers the most uncovered combinations.
   *
   * @param N number of candidates
   */
  public int[] nextBestTestCase(int N) {
	int[] best = nextTestCase();
	if (best == null || N == 1)
	  return best;

	long covBest = model.fitnessValue(best);
	for (int x = 1; x < N; x++) {
	  int[] temp = nextTestCase();
	  long covTemp = model.fitnessValue(temp);
	  // achieve the best fitness
	  if (covTemp == model.getTestCaseCoverMax()) {
		System.arraycopy(temp, 0, best, 0, model.parameter);
		break;
	  } else if (covTemp > covBest) {
		System.arraycopy(temp, 0, best, 0, model.parameter);
		covBest = covTemp;
	  }
	}
	return best;
  }

  /**
   * Return a new test case.
   */
  private int[] nextTestCase() {
	Tuple tp = model.getAnUncoveredTuple();
	if (tp == null)
	  return null;

	// assign the new combination in tc[]
	int[] tc = tp.test;

	// randomize a permutation of other parameters
	List<Integer> permutation = new ArrayList<>();
	for (int k = 0; k < model.parameter; k++) {
	  if (tc[k] == -1)
		permutation.add(k);
	}
	Collections.shuffle(permutation);

	// for each of the remaining parameters
	for (int par : permutation) {
	  tc[par] = selectBestValue(tc, par);
	}

	return tc;
  }

  /**
   * Determine a new parameter-value assignment is constraint satisfiable or not.
   *
   * @param test current partial test case
   * @param p    index of parameter to be assigned
   * @param v    value to be assigned
   */
  private boolean isConstraintSatisfied(final int[] test, int p, int v) {
	int old = test[p];
	test[p] = v;
	boolean satisfied = model.isValid(test);
	test[p] = old;
	return satisfied;
  }

  /**
   * Given a partial test case and a free parameter, return the value assignment
   * that is the best in terms of covering ability and at the same time constraint
   * satisfied.
   *
   * @param test a partial test case
   * @param par  the index of a free parameter
   */
  private int selectBestValue(final int[] test, int par) {
	// iterate all possible values
	ArrayList<Pair> vs = new ArrayList<>();
	for (int i = 0; i < model.value[par]; i++) {
	  if (isConstraintSatisfied(test, par, i)) {
		int num = coveredSchemaNumberFast(test, par, i);
		vs.add(new Pair(i, num));
	  }
	}
	Collections.sort(vs);

	// apply tie-breaking
	int max = vs.get(0).number;
	List<Pair> filtered = vs.stream()
			.filter(p -> p.number == max)
			.collect(Collectors.toList());

	int r = random.nextInt(filtered.size());
	return filtered.get(r).index;
  }

  /**
   * Given a parameter and its corresponding value, return the number of uncovered
   * combinations that can be covered by assigning this parameter value (fast version).
   *
   * @param test current test case before assigning
   * @param par  index of parameter to be assigned
   * @param val  value to be assigned to the parameter
   * @return number of uncovered combinations
   */
  private int coveredSchemaNumberFast(final int[] test, int par, int val) {

	// Only consider the combination between X and the assigned values:
	// iterate all (t-1)-way value combinations among all assigned values
	// to compute the number of uncovered combinations that can be covered
	// by assigning X.
	//
	// 1 1 1 0   X        - - - - -
	// --------  -        ---------
	// assigned  par-val  unassigned

	int fit = 0;
	int count = 0;
	int[] new_test = new int[model.parameter];
	for (int i = 0; i < model.parameter; i++) {
	  new_test[i] = test[i];
	  if (test[i] != -1)
		count++;
	}
	new_test[par] = val;

	int assigned = count;             // number of fixed parameters
	int required = model.t_way - 1;   // number of required parameters to form a t-way combination

	// get fixed part, not including newly assigned one
	int[] fp = new int[assigned];
	int[] fv = new int[assigned];
	for (int i = 0, j = 0; i < model.parameter; i++) {
	  if (new_test[i] != -1 && i != par) {
		fp[j] = i;
		fv[j++] = new_test[i];
	  }
	}

	// newly assigned one
	int[] pp = {par};
	int[] vv = {val};

	// for each possible r-way parameter combinations among fp[]
	for (int[] each : ALG.allCombination(assigned, required)) {
	  int[] pos = new int[required];
	  int[] sch = new int[required];
	  for (int k = 0; k < required; k++) {
		pos[k] = fp[each[k]];
		sch[k] = fv[each[k]];
	  }

	  // construct a temp t-way combination
	  int[] position = new int[model.t_way];
	  int[] schema = new int[model.t_way];
	  mergeArray(pos, sch, pp, vv, position, schema);

	  // determine whether this t-way combination is covered or not
	  if (!model.covered(position, schema, 0))
		fit++;
	}
	return fit;
  }

  /*
   * Merge two sorted arrays into a new sorted array. The ordering is
   * conducted on primary arrays (parameter array), while values in
   * additional arrays (value array) will be exchanged at the same time.
   */
  private static void mergeArray(int[] p1, int[] v1, int[] p2, int[] v2, int[] pos, int[] sch) {
	int i, j, k;
	for (i = 0, j = 0, k = 0; i < p1.length && j < p2.length; ) {
	  if (p1[i] < p2[j]) {
		pos[k] = p1[i];
		sch[k++] = v1[i++];
	  } else {
		pos[k] = p2[j];
		sch[k++] = v2[j++];
	  }
	}
	if (i < p1.length) {
	  for (; i < p1.length; i++, k++) {
		pos[k] = p1[i];
		sch[k] = v1[i];
	  }
	}
	if (j < p2.length) {
	  for (; j < p2.length; j++, k++) {
		pos[k] = p2[j];
		sch[k] = v2[j];
	  }
	}
  }

  public static void main(String[] args) {

	CTModel model = new CTModel(4, new int[]{2, 2, 2, 2}, 2, null, new MFTVerifier());
	// test suite object
	for (int j = 0; j < 1; j++) {
	  TestSuite ts = new TestSuite();
	  AETG gen = new AETG();
	  Instant start = Instant.now();
	  gen.generation(model, ts);
	  Instant end = Instant.now();
	  System.out.println("CA Size = " + ts.size());
	  System.out.println("Time Cost = " + Duration.between(start, end));
	}

  }
}
