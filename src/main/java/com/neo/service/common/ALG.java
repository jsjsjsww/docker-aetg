package com.neo.service.common;

import java.util.*;

public class ALG {

  /**
   * Calculate binomial coefficient c(n, m), where c(n, 0) = 1.
   *
   * @param n number of parameters
   * @param m number of chosen parameters
   * @return c(n, m)
   */
  public static int combine(int n, int m) {
    int ret = 1;
    int p = n;
    for (int x = 1; x <= m; x++, p--) {
      ret = ret * p;
      ret = ret / x;
    }
    return ret;
  }

  /**
   * Calculate the number of all possible value combinations
   * among a given parameter set.
   *
   * @param position indexes of chosen parameters
   * @param value    number of values of all parameters
   * @return number of value combinations among parameters
   */
  public static int combineValue(final int[] position, final int[] value) {
    int comb = 1;
    for (int k = 0; k < position.length; k++)
      comb = comb * value[position[k]];
    return comb;
  }

  /**
   * Calculate the index of a parameter combination in all possible
   * parameter combinations of c(n, m), where index starts at 0.
   *
   * combine2num({1, 2}, 4, 2) = 3,
   * because c(4,2) is as 0 1 , 0 2 , 0 3 , 1 2 , 1 3 , 2 3
   *
   * @param c a parameter combination
   * @param n number of parameters
   * @param m number of chosen parameters
   * @return index of c
   */
  public static int combine2num(final int[] c, int n, int m) {
    int ret = combine(n, m);
    for (int i = 0; i < m; i++) {
      ret -= combine(n - c[i] - 1, m - i);
    }
    return ret - 1;
  }

  /**
   * Calculate the t-th parameter combination of c(n, m),
   * where index starts at 0.
   *
   * num2combine(2, 4, 2) = {0, 3}
   * because c(4,2) is as 0 1 , 0 2 , 0 3 , 1 2 , 1 3 , 2 3
   *
   * @param t index of required parameter combination
   * @param n number of parameters
   * @param m number of chosen parameters
   * @return the t-th parameter combination
   */
  public static int[] num2combine(int t, int n, int m) {
    int[] ret = new int[m];
    t = t + 1;
    int j = 1, k;
    for (int i = 0; i < m; ret[i++] = j++) {
      for (; t > (k = combine(n - j, m - i - 1)); t -= k, j++) ;
    }
    for (int p = 0; p < m; p++)
      ret[p] = ret[p] - 1;
    return ret;
  }

  /**
   * Calculate all parameter combinations of c(n, m).
   * allCombination(4, 2) = {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3}}
   *
   * @param n number of parameters
   * @param m number of chosen parameters
   * @return all parameter combinations, each in a row
   */
  public static ArrayList<int[]> allCombination(int n, int m) {
    ArrayList<int[]> data = new ArrayList<>();
    dfs(data, new int[m], m, 1, n - m + 1, 0);
    return data;
  }

  private static void dfs(List<int[]> data, int[] list, int k_left, int from, int to, int index) {
    if (k_left == 0) {
      data.add(list.clone());
      return;
    }
    for (int i = from; i <= to; ++i ) {
      list[index++] = i - 1;
      dfs(data, list, k_left - 1, i + 1, to + 1, index);
      index -= 1;
    }
  }

  /**
   * Calculate the index of a t-way value combination among
   * given parameters, where index starts at 0.
   *
   * val2num({0, 1}, {1, 2}, 2, {3, 3, 3, 3}) = 5, as the
   * orders of all 3^2 value combinations among parameters {0, 1}
   * are 0 0, 0 1, 0 2, 1 0, 1 1, 1 2, 2 0, 2 1, 2 2
   *
   * @param pos   indexes of chosen parameters
   * @param sch   a value combination
   * @param t     number of chosen parameters
   * @param value number of values of all parameters
   * @return index of sch
   */
  public static int val2num(final int[] pos, final int[] sch, int t, final int[] value) {
    int com = 1;
    int ret = 0;
    for (int k = t - 1; k >= 0; k--) {
      ret += com * sch[k];
      com = com * value[pos[k]];
    }
    return ret;
  }

  /**
   * Calculate the i-th t-way value combination among a given
   * parameter set, where index starts at 0.
   *
   * num2val(4, {1, 2}, 2, {3, 3, 3, 3}) = {1, 1}
   *
   * @param i     index of required value combination
   * @param pos   indexes of chosen parameters
   * @param t     number of chosen parameters
   * @param value number of values of all parameters
   * @return the i-th value combination
   */
  public static int[] num2val(int i, final int[] pos, int t, final int[] value) {
    int[] ret = new int[t];

    int div = 1;
    for (int k = t - 1; k > 0; k--)
      div = div * value[pos[k]];

    for (int k = 0; k < t - 1; k++) {
      ret[k] = i / div;
      i = i - ret[k] * div;
      div = div / value[pos[k + 1]];
    }
    ret[t - 1] = i / div;
    return ret;
  }

  /**
   * Calculate all t-way value combinations among a given parameter set.
   *
   * allV({0, 1}, 2, {3, 3, 3, 3}) =
   * {{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, 1}, {1, 2}, {2, 0}, {2, 1}, {2, 2}}
   *
   * @param pos   indexes of chosen parameters
   * @param t     number of chosen parameters
   * @param value number of values of all parameters
   * @return all value combinations among pos
   */
  public static int[][] allV(final int[] pos, int t, final int[] value) {
    if (t == 0 )
      return new int[1][0];

    int[] counter = new int[t];         // current combination
    int[] counter_max = new int[t];     // the maximum value of each element
    int comb = 1;
    for (int k = 0; k < t; k++) {
      counter[k] = 0;
      counter_max[k] = value[pos[k]] - 1;
      comb = comb * value[pos[k]];
    }
    int end = t - 1;

    int[][] data = new int[comb][t];
    for (int i = 0; i < comb; i++) {
      // assign data[i]
      data[i] = counter.clone();

      // move counter to the next one
      counter[end] = counter[end] + 1;
      int ptr = end;
      while (ptr > 0) {
        if (counter[ptr] > counter_max[ptr]) {
          counter[ptr] = 0;
          counter[--ptr] += 1;
        } else
          break;
      }
    }
    return data;
  }

  /**
   * Calculate the factorial of a non-negative integer.
   *
   * @param t input integer
   * @return t!
   */
  public static int cal_factorial(int t) {
    int n = 1;
    for (int i = 2; i <= t; i++)
      n = n * i;
    return n;
  }

  /**
   * Calculate all permutations of t relations {0, 1, 2, ..., t-1}
   *
   * Reference:
   * [1] The Countdown QuickPerm Algorithm, http://www.quickperm.org/
   *
   * @param t the number of relations
   * @return all permutations
   */
  public static HashMap<ArrayList<Integer>, Integer> cal_permutation(int t) {
    HashMap<ArrayList<Integer>, Integer> permutation = new HashMap<>();
    int count = 0;

    Integer[] v = new Integer[t];
    int[] p = new int[t + 1];
    for (int i = 1; i < t + 1; i++) {
      v[i - 1] = i - 1;
      p[i] = i;
    }

    permutation.put(new ArrayList<>(Arrays.asList(v)), count);
    count = count + 1;

    int i = 1;
    while (i < t) {
      p[i] = p[i] - 1;
      int j = i % 2 * p[i];   // if i is odd then j = p[i] otherwise j = 0
      Integer temp = v[i];    // swap(arr[i], arr[j])
      v[i] = v[j];
      v[j] = temp;

      // display new sequence
      permutation.put(new ArrayList<>(Arrays.asList(v)), count);
      count = count + 1;

      i = 1;
      while (p[i] == 0) {
        p[i] = i;
        i = i + 1;
      }
    }
    return permutation;
  }

  /**
   * Quick sort with ascending order.
   *
   * @param a primary array
   */
  public static void sortArray(int[] a) {
    sortArray(a, 0, a.length - 1);
  }

  public static void sortArray(int[] a, int left, int right) {
    int i, j, temp;
    if (left < right) {
      i = left;
      j = right;
      temp = a[i];
      while (i != j) {
        while (a[j] >= temp && i < j)
          j--;
        if (i < j) {
          a[i] = a[j];
          i++;
        }
        while (a[i] <= temp && i < j)
          i++;
        if (i < j) {
          a[j] = a[i];
          j--;
        }
      }
      a[i] = temp;
      sortArray(a, left, i - 1);
      sortArray(a, i + 1, right);
    }
  }

  /**
   * Quick sort by a and swap corresponding elements in b simultaneously.
   *
   * @param a       primary array
   * @param b       additional array
   * @param version 0 (ascending) or 1 (descending)
   */
  public static void sortArray(int[] a, int[] b, int version) {
    sortArray(a, b, 0, a.length - 1, version);
  }

  public static void sortArray(int[] a, int[] b, int left, int right, int version) {
    int i, j;
    int temp, temp_1;
    if (version == 0 && left < right) {
      i = left;
      j = right;
      temp = a[i];
      temp_1 = b[i];
      while (i != j) {
        while (a[j] >= temp && i < j)
          j--;
        if (i < j) {
          a[i] = a[j];
          b[i] = b[j];
          i++;
        }
        while (a[i] <= temp && i < j)
          i++;
        if (i < j) {
          a[j] = a[i];
          b[j] = b[i];
          j--;
        }
      }
      a[i] = temp;
      b[i] = temp_1;
      sortArray(a, b, left, i - 1, version);
      sortArray(a, b, i + 1, right, version);
    }

    if (version == 1 && left < right) {
      i = left;
      j = right;
      temp = a[i];
      temp_1 = b[i];
      while (i != j) {
        while (a[j] <= temp && i < j)
          j--;
        if (i < j) {
          a[i] = a[j];
          b[i] = b[j];
          i++;
        }
        while (a[i] >= temp && i < j)
          i++;
        if (i < j) {
          a[j] = a[i];
          b[j] = b[i];
          j--;
        }
      }
      a[i] = temp;
      b[i] = temp_1;
      sortArray(a, b, left, i - 1, version);
      sortArray(a, b, i + 1, right, version);
    }
  }

  /**
   * Quick sort by a and swap corresponding elements in b simultaneously.
   *
   * @param a primary array
   * @param b additional 2D array
   */
  public static void sortArray(int[] a, int[][] b) {
    sortArray(a, b, 0, a.length - 1);
  }

  public static void sortArray(int[] a, int[][] b, int left, int right) {
    int i, j;
    int temp;
    int len = b[0].length;
    if (left < right) {
      i = left;
      j = right;
      temp = a[i];
      int[] tpc = new int[len];
      for (int k = 0; k < len; k++)
        tpc[k] = b[i][k];

      while (i != j) {
        while (a[j] > temp && i < j)
          j--;
        if (i < j) {
          a[i] = a[j];
          for (int k = 0; k < len; k++)
            b[i][k] = b[j][k];
          i++;
        }
        while (a[i] < temp && i < j)
          i++;
        if (i < j) {
          a[j] = a[i];
          for (int k = 0; k < len; k++)
            b[j][k] = b[i][k];
          j--;
        }
      }
      a[i] = temp;
      for (int k = 0; k < len; k++)
        b[i][k] = tpc[k];

      sortArray(a, b, left, i - 1);
      sortArray(a, b, i + 1, right);
    }
  }

  /**
   * Determine whether an integer array exists in an ArrayList
   *
   * @param list target ArrayList
   * @param t    candidate array
   * @return true or false
   */
  public static boolean inList(final ArrayList<int[]> list, final int[] t) {
    for (int[] item : list) {
      if (Arrays.equals(item, t)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determine whether t[] exist in the first bound-th rows of array[][]
   *
   * @param array two dimensional array
   * @param bound integer
   * @param t     one dimensional array
   * @return true or false
   */
  public static boolean inArray(final int[][] array, int bound, final int[] t) {
    for (int i = 0; i < bound; i++) {
      if (Arrays.equals(array[i], t))
        return true;
    }
    return false;
  }

}

