package edu.westga.cs3152.model;

import java.io.IOException;

/**
 * Class CNF
 * 
 * Represents a CNF formula and an assignment to its logic variables. The
 * assignment may be partial, i.e.e not all variables have to be assigned a
 * truth value. Value 1 represents TRUE, value -1 represents FALSE, and 0 is
 * used to represent no current value.
 * 
 * @author CS3152
 * @version Fall 2021
 */
public class CNF {
	private DimacParser parser;
	private int isFormulaTrue;
	private int[] variables;
	
	/**
	 * Instantiates a new CNF object and sets it to the CNF formula specified in the
	 * given file. The file conforms to the simplified DIMACS format: (Adapted from
	 * http://www.satcompetition.org/2009/format-benchmarks2009.html)
	 * 
	 * The file can start with comment lines, but does not have to. A comment line
	 * starts with the character c in the first column.
	 * 
	 * Right after the comments, there is the line "p cnf nbvar nbclauses"
	 * indicating that the instance is in CNF format; nbvar is the exact number of
	 * variable appearing in the file; nbclauses is the exact number of clauses
	 * contained in the file.
	 * 
	 * Then the clauses follow. Each clause is a sequence of distinct non-null
	 * numbers between -nbvar and nbvar ending with 0 on the same line; it cannot
	 * contain the opposite literals i and -i simultaneously and it cannot contain
	 * the same literal twice. Positive numbers denote the corresponding variables.
	 * Negative numbers denote the negations of the corresponding variables.
	 * 
	 * @pre filename is the name of a file which must meet the simplified DIMACS
	 *      format
	 * @post This CNF object represents the CNF formula specified in the given file.
	 *       No variables are set to a value.
	 * @param filename the name of the file with the CNF instance
	 * @throws IllegalArgumentException if the specified file does not exist or if
	 *                                  the specified file does not meet the DIMACS
	 *                                  format
	 */
	public CNF(String filename) {
		try {
			this.parser = new DimacParser();
			this.parser.parseCnf(filename);
			this.variables = new int[this.parser.getVariables()];
			this.isFormulaTrue = this.isFormulaTrue();
		} catch (IOException exception) {
			throw new IllegalArgumentException(exception.getMessage());
		}
	}

	/**
	 * Gets the number of logic variables in this CNF formula.
	 * 
	 * @pre none
	 * @post none
	 * @return number variables
	 */
	public int numberVariables() {
		return this.parser.getVariables();
	}

	/**
	 * Gets the number of clauses of this CNF formula.
	 * 
	 * @pre none
	 * @post none
	 * @return number clauses
	 */
	public int numberClauses() {
		return this.parser.getClauses();
	}

	/**
	 * Gets the value of the formula under the current assignment. The returned
	 * value is 1 if all clauses are true, -1 if one or more clauses are false, and
	 * 0 otherwise
	 * 
	 * @pre none
	 * @post none
	 * @return the current value of this formula
	 */
	public int getValue() {
		return this.isFormulaTrue;
	}

	/**
	 * Gets the current value of the specified variable.
	 * 
	 * @pre none
	 * @post none
	 * @param var the variable of which the value is returned
	 * @return the value of this variable
	 */
	public int getValue(int var) {
		int varOffset = var - 1;
		if (varOffset > this.variables.length - 1 && varOffset < 0) {
			return 0;
		}
		return this.variables[varOffset];
	}

	/**
	 * Sets the specified variable to the specified value and updates the value of
	 * this CNF formula.
	 * 
	 * @pre 1 <= var <= numberVariables() AND val is element of {-1, 1} AND
	 *      getValue(var) = 0
	 * @post getValue(var) = val
	 * @param var variable to be set
	 * @param val the new variable value
	 * @return 1 if all clauses are true, -1 if one or more clauses are false, and 0
	 *         otherwise
	 * @throws IllegalArgumentException if the precondition is not met
	 */
	public int set(int var, int val) {
		int varOffset = var - 1;
		if (varOffset < 0 || varOffset > this.parser.getVariables() - 1) {
			throw new IllegalArgumentException("Variable out of bounds");
		}
		if (val != -1 && val != 1) {
			throw new IllegalArgumentException("Value not -1 or 1");
		}
		if (this.getValue(var) != 0) {
			throw new IllegalArgumentException("Variable is already set");
		}
		for (int index = 0; index < this.variables.length; index++) {
			if (index == varOffset) {
				this.variables[index] = val;
			}
		}
		this.isFormulaTrue = this.isFormulaTrue();
		return this.isFormulaTrue;
	}

	/**
	 * Unsets the specified variable and updates the value of this CNF formula.
	 * 
	 * @pre 1 <= var <= numberVariables() AND getValue(var) is element of {-1, 1}
	 * @post getValue(var) = 0
	 * @param var index of variable to be set
	 * @return 1 if all clauses are satisfied, -1 if one or more clauses are false,
	 *         and 0 otherwise
	 * @throws IllegalArgumentException if the precondition is not met
	 */
	public int unset(int var) {
		int varOffset = var - 1;
		if (varOffset < 0 || varOffset > this.parser.getVariables() - 1) {
			throw new IllegalArgumentException("Variable out of bounds");
		}
		if (this.getValue(var) == 0) {
			throw new IllegalArgumentException("Variable has not been set");
		}
		for (int index = 0; index < this.variables.length; index++) {
			if (index == varOffset) {
				this.variables[index] = 0;
			}
		}
		this.isFormulaTrue = this.isFormulaTrue();
		return this.isFormulaTrue;
	}
	
	private int isFormulaTrue() {
		int[][] clauses = this.parser.getClauseInstances();
		int[] clauseEvaluation = new int[this.parser.getClauses()];
		for (int var = 0; var < clauses.length; var++) {
			for (int clause = 0; clause < clauses[var].length; clause++) {
				int clauseValue = clauses[var][clause];
				int valueOfVar = this.variables[var];
				clauseEvaluation[clause] = (valueOfVar * clauseValue);
			}
		}
		int trueOrCantBeDetermined = 1;
		for (int clause = 0; clause < clauseEvaluation.length; clause++) {
			if (clauseEvaluation[clause] < 0) {
				return -1;
			}
			if (clauseEvaluation[clause] == 0) {
				trueOrCantBeDetermined = 0;
			}
		}
		return trueOrCantBeDetermined;
	}
	
}