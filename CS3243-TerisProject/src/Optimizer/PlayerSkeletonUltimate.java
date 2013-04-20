package Optimizer;
import java.util.concurrent.*;
public class PlayerSkeletonUltimate implements Callable<FitParameters>{
	/*
	 * Debugging parameters
	 */
	public int iteration = 0;
	public int playerNo = 0;
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
	double[] weight;
	
	// Fitness parameters
	public long L = 0;
	public long Pmax = 0;
	public long Psum = 0;
	public long Hmax = 0;
	public long Hsum = 0;
	public long Rmax = 0;
	public long Rsum = 0;
	public long Cmax = 0;
	public long Csum = 0;
	public long count = 0;

	/**
	 * Constructor
	 * 
	 * @param LH
	 *            landing height weight
	 * @param RowClear
	 *            cleared row weight
	 * @param RT
	 *            row transition weight
	 * @param CT
	 *            column transition weight
	 * @param Hole
	 *            hole number weight
	 * @param Well
	 *            well number weight
	 * @param PH
	 *            pile height weight
	 */
	public PlayerSkeletonUltimate(double LH, double RowClear, double RT, double CT,
			double Hole, double Well, double PH) {
		landheight 			= LH;
		rowclear 			= RowClear;
		rowtransition 		= RT;
		columntransition 	= CT;
		holenumber 			= Hole;
		wellnumber 			= Well;
		pileheight 			= PH;
	}

	/**
	 * Constructor
	 */
	public PlayerSkeletonUltimate() {// initial weight
		landheight 			= -3.3200740;
		rowclear 			= 2.70317569;
		rowtransition 		= -2.7157289;
		columntransition 	= -5.1061407;
		holenumber 			= -6.9380080;
		wellnumber 			= -2.4075407;
		pileheight 			= -1.0;// feature added
	}

	public PlayerSkeletonUltimate(double[] parameters) {
		landheight = parameters[0];
		rowclear = parameters[1];
		rowtransition = parameters[2];
		columntransition = parameters[3];
		holenumber = parameters[4];
		wellnumber = parameters[5];
		pileheight = parameters[6];
	}
	
	public PlayerSkeletonUltimate(double[] parameters, int iteration, int playerNo) {
		landheight = parameters[0];
		rowclear = parameters[1];
		rowtransition = parameters[2];
		columntransition = parameters[3];
		holenumber = parameters[4];
		wellnumber = parameters[5];
		pileheight = parameters[6];
		this.iteration = iteration;
		this.playerNo = playerNo;
	}
	
	/**
	 * pick the move with best utility
	 * @param s 
	 * 			current state
	 * @param legalMoves
	 * 			all the legal moves
	 * @return
	 * 			chosen move with best utility
	 */
	public int pickMove(State s, int[][] legalMoves) {
		double[] eval = new double[legalMoves.length];
		int maxId = 0;
		
		
		for (int i = 0; i<eval.length; i++) {
			//reward
			
			//utility function
			eval[i] = utilityFunction_v2(s, legalMoves[i]);
			
			if ( (i > 0) && (eval[i] > eval[maxId]) )
				maxId = i;
		}
	
		return maxId;
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

	/**
	 * main function
	 */
	public void play() {
		State s = new State();
		//new TFrame(s);
		while (!s.hasLost()) {
			/*
			if (count > 50000) {
				break;
			}// for debugging
			*/
			s.makeMove(pickMove(s, s.legalMoves()));
			/*
			 * Four Parameter: useful for PSO
			 */
			// Join
			int pileHeight = getMax(s.getTop());
			int numHoles = GetNumberOfHoles(s.getTop(), s.getField());
			int rowTransit = GetRowTransitions(s.getField());
			int colTransit = GetColumnTransitions(s.getField());
			
			
			Pmax = Math.max(Pmax, pileHeight);
			Psum += pileHeight;
			Hmax = Math.max(Hmax, numHoles);
			Hsum += numHoles;
			Rmax = Math.max(Rmax, rowTransit);
			Rsum += rowTransit;
			Cmax = Math.max(Cmax, colTransit);
			Csum += colTransit;
			count++;

			 //s.draw();
			 //s.drawNext(0, 0);
//			System.out.println("new   " + s.getRowsCleared());
			
			if (count == 1000) {
				System.out.println("Iteration " + iteration + " Player " + playerNo + " Now cleared: " + s.getRowsCleared());//for debugging
				return;
			}
			
			/*
			  try { Thread.sleep(3); } catch (InterruptedException e) {
			  e.printStackTrace(); }
			  */
			 
		}
		L = s.getRowsCleared();
		//System.out.println("You have completed " + s.getRowsCleared() + " rows.");
	}

	/**
	 * compute
	 * Used for optimization using PSO
	 */
	@Override
	public FitParameters call(){
		play();
		return new FitParameters(L, Pmax, Psum, Hmax, Hsum, Rmax, Rsum, Cmax, Csum, count);
	}
	
	static public void main(String[] args) {
		PlayerSkeletonUltimate player = new PlayerSkeletonUltimate();
		player.play();
	}
}
