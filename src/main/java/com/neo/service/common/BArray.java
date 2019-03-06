package com.neo.service.common;

import java.util.Random;

/**
 * A two dimensional array where each element is a boolean variable.
 */
public class BArray {

  private boolean[][] matrix;
  private Random random = new Random();

  // zero positions: each zero position is represented as a pair <row, column>
  private int[] zero_row;
  private int[] zero_column;
  private int zero_total;

  public BArray(int row) {
    matrix = new boolean[row][];
    zero_total = 0 ;
    for (int i = 0; i < row; i++)
      matrix[i] = null;
  }

  public BArray(int row, int column) {
    matrix = new boolean[row][column];
    zero_total = row * column;
    zero_row = new int[zero_total];
    zero_column = new int[zero_total];
    int index = 0;
    for (int i = 0; i < row; i++) {
      for (int j = 0; j < column; j++) {
        matrix[i][j] = false;
        zero_row[index] = i;
        zero_column[index] = j;
        index += 1;
      }
    }
  }

  public boolean[][] getMatrix(){
    return matrix;
  }

  /**
   * Initialize a single row by zero.
   *
   * @param index  index of row
   * @param column number of elements in that row
   */
  public void initializeRow(int index, int column) {
    if (matrix[index] == null) {
      matrix[index] = new boolean[column];
      for (int j = 0; j < column; j++)
        matrix[index][j] = false;
      zero_total += column;
    }
  }

  /**
   * If the matrix is initialized by row, then this method should be
   * used to initialize the zero positions.
   */
  public void initializeZeros() {
    zero_row = new int[zero_total];
    zero_column = new int[zero_total];
    int index = 0;
    for (int i = 0 ; i < matrix.length ; i++ ) {
      for (int j = 0 ; j < matrix[i].length ; j++) {
        zero_row[index] = i;
        zero_column[index] = j;
        index += 1;
      }
    }
  }

  /**
   * Get the element in row i and column j.
   *
   * @param i index of row
   * @param j index of column
   * @return false or true
   */
  public boolean getElement(int i, int j) {
    return matrix[i][j];
  }

  /**
   * Set the element in row i and column j to true or false.
   *
   * @param i     index of row
   * @param j     index of column
   * @param value new value
   */
  public void setElement(int i, int j, boolean value) {
    matrix[i][j] = value;
  }

  /**
   * Convert a specified row into a string representation.
   *
   * @param index  index of row
   * @return string representation
   */
  public String getRow(int index) {
    StringBuilder sb = new StringBuilder();
    for (boolean b : matrix[index]) {
      String str = b ? "1 " : "0 ";
      sb.append(str);
    }
    return sb.toString();
  }

  /**
   * Return the index of row and column of a random zero position.
   */
  public Position getRandomZeroPosition() {
    while (zero_total > 0) {
      int index = random.nextInt(zero_total);
      int row = zero_row[index];
      int column = zero_column[index];
      if (matrix[row][column]) {
        // move the pair to the last position (i.e. index = zero_total - 1)
        int right = zero_total - 1;
        zero_row[index] = zero_row[right];
        zero_column[index] = zero_column[right];
        zero_total -= 1;
      }
      else {
        return new Position(row, column);
      }
    }
    return null;
  }
}

