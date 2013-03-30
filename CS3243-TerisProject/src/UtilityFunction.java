import java.util.concurrent.*;
public class UtilityFunction extends RecursiveTask<Double> {
	/*
	 * weight parameter
	 */
	double landheight;
	double rowclear;
	double rowtransition;
	double columntransition;
	double holenumber;
	double wellnumber;
	double pileheight;
	State s;
	int[] move;
	
	public UtilityFunction(State s, int[] move, double[] parameters) {
		landheight = parameters[0];
		rowclear = parameters[1];
		rowtransition = parameters[2];
		columntransition = parameters[3];
		holenumber = parameters[4];
		wellnumber = parameters[5];
		pileheight = parameters[6];
		this.s = s;
		this.move = move;
	}
	
	/**
	 * Calculate the utility of each legal move
	 * 
	 * @param s
	 *            : current state
	 * @param move
	 *            : the chosen move
	 * @return the utility of the chosen move for curretn state
	 */
	public double utilityFunction_v2(State s, int[] move) {

		// simulate make move
		
		int completed 		= s.getRowsCleared();
		int orient 			= move[State.ORIENT];
		int slot 			= move[State.SLOT];
		int nextPiece 		= s.getNextPiece();
		int turn 			= s.getTurnNumber()+1;
		
		int[] top 			= s.getTop();
		int[] topTemp 		= new int[top.length];
		int[][] field 		= s.getField();
		int[][] fieldTemp 	= new int[field.length][field[0].length];
		int[][] pWidth 		= State.getpWidth();
		int[][] pHeight 	= State.getpHeight();
		int[][][] pTop 		= State.getpTop();
		int[][][] pBottom	= State.getpBottom();
		

		for (int i = 0; i < top.length; i++)
			topTemp[i] = top[i];

		for (int i = 0; i < field.length; i++)
			for (int j = 0; j < field[0].length; j++)
				fieldTemp[i][j] = field[i][j];

		top 	= topTemp;
		field 	= fieldTemp;
		
//====================simulating makeMove=======================================
		
		// height if the first column makes contact
		int height = top[slot] - pBottom[nextPiece][orient][0];

		// for each column beyond the first in the piece
		for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
			height = Math.max(height, top[slot + c]
					- pBottom[nextPiece][orient][c]);
		}
		int rowsCleared = 0;

		// check if game ended
		if (height + pHeight[nextPiece][orient] < State.ROWS) { // Li Chenhao

			// for each column in the piece - fill in the appropriate blocks
			for (int i = 0; i < pWidth[nextPiece][orient]; i++) {

				// from bottom to top of brick
				for (int h = height + pBottom[nextPiece][orient][i]; h < height
						+ pTop[nextPiece][orient][i]; h++) {
					field[h][i + slot] = turn;
				}
			}

			// adjust top
			for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
				top[slot + c] = height + pTop[nextPiece][orient][c];
			}

			// check for full rows - starting at the top
			for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
				// check all columns in the row
				boolean full = true;
				for (int c = 0; c < State.COLS; c++) {
					if (field[r][c] == 0) {
						full = false;
						break;
					}
				}
				// if the row was full - remove it and slide above stuff down
				if (full) {
					rowsCleared++;
					completed++;
					// for each column
					for (int c = 0; c < State.COLS; c++) {

						// slide down all bricks
						for (int i = r; i < top[c]; i++) {
							field[i][c] = field[i + 1][c];
						}
						// lower the top
						top[c]--;
						while (top[c] >= 1 && field[top[c] - 1][c] == 0)
							top[c]--;
					}
				}
			}
		} else
			return -0xffff;// a small value;
//===================================================================================

		double result = GetLandingHeight(pHeight[nextPiece][orient], height) * landheight
				+ rowsCleared * rowclear + GetRowTransitions(field)
				* rowtransition + GetColumnTransitions(field)
				* columntransition + GetNumberOfHoles(top, field)
				* holenumber + GetWellSums(top, field)
				* wellnumber+getMax(top)*pileheight;
		return result;
	}
	

	/**
	 * get the max column height of the board
	 * 
	 * @param top
	 *            :top array of state
	 * @return max column height
	 */
	private int getMax(int[] top) {
		int max = top[0];

		for (int i = 0; i < top.length; i++) {
			max = Math.max(max, top[i]);
		}

		return max;
	}

	/**
	 * Get landing height of a piece
	 * 
	 * @param pHeight
	 *            : height of the piece
	 * @param height
	 *            : height of landing point
	 * @return landing height of a piece
	 */
	public int GetLandingHeight(int pHeight, int height) {
		int LH = 0;

		LH = height + ((pHeight-1) / 2);
		return LH;
	}

	/**
	 * Get row transition number
	 * 
	 * @param board
	 *            :field array in state
	 * @return row transition number
	 */
	public int GetRowTransitions(int[][] board) {
		int RT = 0;
		int previous_state = 1;

		for (int row = 0; row < State.ROWS-1; row++) {    
			for (int col = 0; col < State.COLS; col++) {
				if ((board[row][col] !=0)!= (previous_state!=0)) {
					RT++;
				}
				previous_state = board[row][col];
			}
			if (board[row][State.COLS - 1] == 0)
				RT++;
			previous_state = 1;
		}
		return RT;
	}

	/**
	 * Get column transition number
	 * 
	 * @param board
	 *            :field array in state
	 * @return column transition number
	 */
	public int GetColumnTransitions(int[][] board) {
		int CT = 0;
		int previous_state = 1;

		for (int col = 0; col < State.COLS; col++) {
			for (int row = 0; row < State.ROWS-1; row++) {					
				if ((board[row][col] !=0)!= (previous_state!=0)) {
					CT++;
				}
				
				if (board[State.ROWS-1][col] == 0)                          //Li Chenhao
					CT++;
				previous_state = board[row][col];
			}
			previous_state = 1;
		}
		return CT;
	}

	/**
	 * Get nubmer of holes
	 * 
	 * @param top
	 *            :top array in state
	 * @param board
	 *            :field array in state
	 * @return : number of holes of given state
	 */
	public int GetNumberOfHoles(int[] top, int[][] board) {
		int holes = 0;
		
		for (int j = 0; j < State.COLS; j++) {
			for (int i = 0; i < top[j] - 1; i++) {
				if (board[i][j] == 0)
					holes++;
			}
		}
		return holes;
	}

	/**
	 * Get number of wells (with depth accumulated)
	 * 
	 * @param top
	 *            :top array in state
	 * @param board
	 *            :field array in state
	 * @return : number of wells(with depth accumulated)
	 */
	public int GetWellSums(int[] top, int[][] board) {
		int well = 0;
		for (int col = 1; col < State.COLS - 1; col++) {
			for (int row = State.ROWS - 2; row >= 0; row--) {   
				if (board[row][col] == 0 && board[row][col - 1] != 0
						&& board[row][col + 1] != 0) {
					well++;
					for (int i = row - 1; i >= 0; i--) {
						if (board[i][col] == 0) {
							well++;
						} else {
							break;
						}
					}
				}
			}
		}

		for (int row = State.ROWS - 2; row >= 0; row--) {     
			if (board[row][0] == 0 && board[row][1] != 0) {
				well++;
				for (int i = row - 1; i >= 0; i--) {
					if (board[i][0] == 0) {
						well++;
					} else {
						break;
					}
				}
			}
		}

		for (int row = State.ROWS - 2; row >= 0; row--) {    
			if (board[row][State.COLS - 1] == 0
					&& board[row][State.COLS - 2] != 0) {
				well++;
				for (int i = row - 1; i >= 0; i--) {
					if (board[i][State.COLS - 1] == 0) {
						well++;
					} else {
						break;
					}
				}
			}
		}
		return well;
	}

	@Override
	protected Double compute() {
		return utilityFunction_v2(s, move);
	}
}
