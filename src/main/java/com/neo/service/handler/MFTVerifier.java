package com.neo.service.handler;

import com.neo.service.combinatorial.CTModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class MFTVerifier implements ValidityChecker {

  private int[][] relation;
  public Vector<Constraint> MFT;

  public MFTVerifier() {
	MFT = new Vector<>();
  }

  /**
   * Calculate minimum forbidden tuple.
   * @param model an object of CT model
   */
  public void init(CTModel model) {
	if (model.constraint == null)
	  return;

	relation = model.relation;
	Vector<Constraint> originalConstraint = new Vector<>();
	for (int[] x : model.constraint) {
	  int[] y = sort(x);
	  originalConstraint.add(new Constraint(y));
	}
	minimise(originalConstraint);

	// calculate all implicit parameters
	// all values of an implicit parameter are involved in constraints
	Vector<Integer> imParameter = new Vector<>();
	for (int i = 0; i < relation.length; i++) {
	  boolean isImParameter = true;
	  for (int j = 0; j < relation[i].length; j++) {
		boolean getMatchCons = false;
		for (Constraint cons : originalConstraint) {
		  for (int k = 0; k < cons.disjunction.length; k++)
			if (relation[i][j] == Math.abs(cons.disjunction[k])) {
			  getMatchCons = true;
			  break;
			}
		  if (getMatchCons)
			break;
		}
		if (!getMatchCons) {
		  isImParameter = false;
		  break;
		}
	  }
	  if (isImParameter)
		imParameter.add(i);
	}

	Vector<Integer> imParameter1 = new Vector<>(imParameter);
	Vector<Constraint> newConstraint = new Vector<>();

	// while there is an implicit parameter
	while (imParameter.size() != 0) {
	  Vector<Integer> tempParameter = new Vector<>();
	  newConstraint.clear();

	  // for every implicit parameter, calculate the valid Cartesian product of all tuples
	  for (Integer ip : imParameter) {

		Vector<Constraint> cartesian = new Vector<>();
		// for each value of an implicit parameter
		for (int ii = 0; ii < relation[ip].length; ii++) {

		  // get all constraints that contain this value (this value is not included in tmpCons)
		  Vector<Constraint> tmpCons = new Vector<>();
		  for (Constraint cons : originalConstraint) {
			for (int k = 0; k < cons.disjunction.length; k++)
			  // get a matched constraint
			  if (Math.abs(cons.disjunction[k]) == relation[ip][ii]) {
				int[] tmpArray = new int[cons.disjunction.length - 1];
				int tmpI = 0, tmp1 = 0;
				while (tmpI < cons.disjunction.length) {
				  if (tmpI != k) {
					tmpArray[tmp1] = cons.disjunction[tmpI];
					tmp1++;
				  }
				  tmpI++;
				}
				tmpCons.add(new Constraint(tmpArray));
				break;
			  }
		  }

		  // if this is the first value of the implicit parameter
		  if (ii == 0) {
			cartesian.addAll(tmpCons);
		  }
		  // calculate Cartesian product of tuples in current cartesian set and the obtained tmpCons set
		  else {
			Vector<Constraint> tmp = new Vector<>(cartesian);
			cartesian.clear();

			for (Constraint cons1 : tmp)
			  for (Constraint cons2 : tmpCons) {
				int x = 0, y = 0, z = 0;
				// combine the two tuples into a new tuple mergeCons
				int[] mergeCons = new int[cons1.disjunction.length + cons2.disjunction.length];
				while (x < cons1.disjunction.length || y < cons2.disjunction.length) {
				  if (x >= cons1.disjunction.length) {
					mergeCons[z] = cons2.disjunction[y];
					y++;
				  } else if (y >= cons2.disjunction.length) {
					mergeCons[z] = cons1.disjunction[x];
					x++;
				  } else if (cons1.disjunction[x] > cons2.disjunction[y]) {
					mergeCons[z] = cons1.disjunction[x];
					x++;
				  } else {
					mergeCons[z] = cons2.disjunction[y];
					y++;
				  }
				  z++;
				}

				// check the validity of this merged tuple
				y = 0;
				boolean drop = false;
				for (x = 0; x < mergeCons.length - 1; x++) {
				  while (Math.abs(mergeCons[x]) > relation[y][relation[y].length - 1])
					y++;
				  // get two different values for a same parameter
				  if (Math.abs(mergeCons[x + 1]) <= relation[y][relation[y].length - 1] && mergeCons[x] != mergeCons[x + 1]) {
					drop = true;
					break;
				  }
				}

				// if it is a valid and non-duplicated tuple, save it
				if (!drop) {
				  List<Integer> list = new ArrayList<>();
				  for (x = 0; x < mergeCons.length; x++) {
					if (!list.contains(mergeCons[x])) {
					  list.add(mergeCons[x]);
					}
				  }
				  int[] deducedCons = new int[list.size()];
				  for (x = 0; x < list.size(); x++) {
					deducedCons[x] = list.get(x);
				  }
				  // if it is not in cartesian
				  if (!isDuplicated(cartesian, deducedCons))
					cartesian.add(new Constraint((deducedCons)));
				}
			  }
		  } // end else
		} // end for each value

		newConstraint.addAll(cartesian);
	  }  // end for every implicit parameter

	  // store new constraints and calculate implicit parameters for the next round
	  for (Constraint consN : newConstraint) {
		// check whether the constraint consN is duplicated
		boolean isNew = true;
		for (Constraint consB : originalConstraint) {
		  if (consB.isSuperior(consN)) {
			isNew = false;
			break;
		  }
		}
		if (isNew) {
		  originalConstraint.add(consN);
		  int k = 0;
		  for (int y = 0; y < consN.disjunction.length; y++) {
			// if a new constraint is added, and it contains an implicit parameter
			// then include this parameter in next round
			while (Math.abs(consN.disjunction[y]) > relation[k][relation[k].length - 1])
			  k++;
			if (imParameter1.contains(k) && !tempParameter.contains(k))
			  tempParameter.add(k);
		  }
		}
	  }

	  // update the new implicit parameters
	  imParameter.clear();
	  imParameter.addAll(tempParameter);

	  // minimise constraints
	  minimise(originalConstraint);

	} // end the outer while loop

	// update MFT
	MFT = new Vector<>();
	for (Constraint cons : originalConstraint) {
	  int[] sort = sort(cons.disjunction);
	  MFT.add(new Constraint(sort));
	}
	//System.out.println("MFT size = " + MFT.size());
  }

  /**
   * Return a sorted array of non-increasing order.
   */
  private int[] sort(final int[] a) {
	int[] b = a.clone();
	for (int i = 1; i < b.length; i++) {
	  int key = b[i];
	  int j = i - 1;
	  while (j >= 0 && b[j] < key) {
		b[j + 1] = b[j];
		j = j - 1;
	  }
	  b[j + 1] = key;
	}
	return b;
  }

  /**
   * Determine whether A is contained in the set of constraint V.
   */
  private boolean isDuplicated(Vector<Constraint> V, int[] A) {
	for (int i = 0; i < V.size(); i++) {
	  int[] b = V.get(i).disjunction;
	  boolean re;
	  if (b.length == A.length) {
		re = true;
		for (int j = 0; j < b.length; j++) {
		  if (b[j] != A[j]) {
			re = false;
			break;
		  }
		}
		if (re)
		  return true;
	  }
	}
	return false;
  }

  /**
   * Minimise the given set of constraints: only keep the minimal constraint
   * in the constraint set.
   */
  private void minimise(Vector<Constraint> constraintSet) {
	// the indexes of to-be-removed constraints
	ArrayList<Integer> removeList = new ArrayList<>();
	for (int i = 0; i < constraintSet.size() - 1; i++) {
	  for (int j = i + 1; j < constraintSet.size(); j++) {
		Constraint c1 = constraintSet.get(i), c2 = constraintSet.get(j);
		// if the two constraints are involved in each other
		// then remove the large one
		if (check(c1, c2)) {
		  if (c1.disjunction.length <= c2.disjunction.length) {
			if (!removeList.contains(j))
			  removeList.add(j);
		  } else {
			if (!removeList.contains(i))
			  removeList.add(i);
		  }
		}
	  }
	}
	//remove constraint, begin with large index
	Collections.sort(removeList, Collections.reverseOrder());
	for (int i = 0; i < removeList.size(); i++) {
	  constraintSet.removeElementAt(removeList.get(i));
	}
  }

  /**
   * Given two constraints (disjunction form), determine whether one is
   * involved in another.
   */
  private boolean check(Constraint A, Constraint B) {
	// ensure tht |X| <= |Y|
	Constraint X, Y;
	if (A.disjunction.length <= B.disjunction.length) {
	  X = A;
	  Y = B;
	} else {
	  X = B;
	  Y = A;
	}

	boolean match = true;
	for (int i = 0; i < X.disjunction.length; i++) {
	  int j;
	  for (j = 0; j < Y.disjunction.length; j++)
		if (X.disjunction[i] == Y.disjunction[j])
		  break;
	  if (j == Y.disjunction.length && X.disjunction[i] != Y.disjunction[Y.disjunction.length - 1]) {
		match = false;
		break;
	  }
	}
	return match;
  }

  /**
   * Determine whether a given complete or partial test case is
   * constraints satisfiable. Any free parameters are assigned
   * to value -1.
   *
   * @param test a complete or partial test case
   */
  public boolean isValid(final int[] test) {
	if (MFT.size() == 0)
	  return true;

	// convert the test case to constraint's form
	// for example: value = [2,2,2]
	// TC = [0, 0, -1] will be converted to [1, 3, -1]
	int[] TC = new int[test.length];
	int baseNum = 0;
	for (int i = 0; i < test.length; i++) {
	  if (test[i] != -1)
		TC[i] = test[i] + baseNum + 1;
	  else
		TC[i] = -1;
	  baseNum += relation[i].length;
	}

	// find if the input TC contains any constraint
	for (Constraint cons : MFT) {
	  boolean matched = true;
	  int k = 0;
	  int j = 0;
	  while (j < cons.disjunction.length) {
		while (k < TC.length && (TC[k] == -1 || Math.abs(cons.disjunction[j]) > TC[k])) {
		  k++;
		  if (k == TC.length)
			matched = false;
		}
		if (!matched)
		  break;
		if (Math.abs(cons.disjunction[j]) == TC[k]) {
		  k++;
		  j++;
		} else {
		  matched = false;
		  break;
		}
	  }
	  if (matched) {
		return false;
	  }
	}
	return true;
  }


  public static void main(String[] args) {/*
	ArrayList<int[]> cons = new ArrayList<>();
	int[] cons1 = new int[2];
	cons1[0] = -1;
	cons1[1] = -4;
	int[] cons2 = new int[2];
	cons2[0] = -1;
	cons2[1] = -6;
	int[] cons3 = new int[2];
	cons3[0] = -2;
	cons3[1] = -10;
	int[] cons4 = new int[2];
	cons4[0] = -3;
	cons4[1] = -10;
	int[] cons5 = new int[2];
	cons5[0] = -5;
	cons5[1] = -10;
	cons.add(cons1);
	cons.add(cons2);
	cons.add(cons3);
	cons.add(cons4);
	cons.add(cons5);
	int[] parameter = new int[4];
	parameter[0] = 3;
	parameter[1] = 3;
	parameter[2] = 3;
	parameter[3] = 3;
	Solver chso = new Solver();
	CTModel test = new CTModel(4, parameter, 2, cons, chso);

	MFTVerifier test1 = new MFTVerifier();
	test1.init(test);
	//System.out.println(test1.MFT.size());
	for (int i = 0; i < test1.MFT.size(); i++)
	  for (int j = 0; j < test1.MFT.get(i).disjunction.length; j++)
		System.out.print(test1.MFT.get(i).disjunction[j] + " ");
	int[] testcase = new int[4];
	testcase[0] = 0;
	testcase[1] = 2;
	testcase[2] = 2;
	testcase[3] = 1;
	System.out.println(test1.isValid(testcase));*/
  }

}
