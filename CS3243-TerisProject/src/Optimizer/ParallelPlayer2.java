package Optimizer;
import java.util.concurrent.*;
import java.util.*;


public class ParallelPlayer2 {
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
	double[] parameters;
	
	// Fitness parameters
	public int L = 0;
	public int Pmax = 0;
	public int Psum = 0;
	public int Hmax = 0;
	public int Hsum = 0;
	public int Rmax = 0;
	public int Rsum = 0;
	public int Cmax = 0;
	public int Csum = 0;
	public int count = 0;
	
	ForkJoinPool mainPool = new ForkJoinPool(10);

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
	public ParallelPlayer2(double LH, double RowClear, double RT, double CT,
			double Hole, double Well, double PH) {
		landheight 			= LH;
		rowclear 			= RowClear;
		rowtransition 		= RT;
		columntransition 	= CT;
		holenumber 			= Hole;
		wellnumber 			= Well;
		pileheight 			= PH;
		parameters = new double[7];
		parameters[0] = landheight;
		parameters[1] = rowclear;
		parameters[2] = rowtransition;
		parameters[3] = columntransition;
		parameters[4] = holenumber;
		parameters[5] = wellnumber;
		parameters[6] = pileheight;
	}

	/**
	 * Constructor
	 */
	public ParallelPlayer2() {// initial weight
		landheight 			= -3.3200740;
		rowclear 			= 2.70317569;
		rowtransition 		= -2.7157289;
		columntransition 	= -5.1061407;
		holenumber 			= -6.9380080;
		wellnumber 			= -2.4075407;
		pileheight 			= -1.0;// feature added
		parameters = new double[7];
		parameters[0] = landheight;
		parameters[1] = rowclear;
		parameters[2] = rowtransition;
		parameters[3] = columntransition;
		parameters[4] = holenumber;
		parameters[5] = wellnumber;
		parameters[6] = pileheight;
	}

	/**
	 * Constructor
	 * 
	 * Pass in an array of doubles and assign to weights
	 * accordingly
	 */
	public ParallelPlayer2(double[] parameters) {
		landheight = parameters[0];
		rowclear = parameters[1];
		rowtransition = parameters[2];
		columntransition = parameters[3];
		holenumber = parameters[4];
		wellnumber = parameters[5];
		pileheight = parameters[6];
		this.parameters = parameters;
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
		ArrayList<Future<Double>> futureList = new ArrayList<Future<Double>>(eval.length);
		
		for (int i = 0; i < eval.length; ++i) {
			futureList.add(mainPool.submit(new UtilityFunction(s, legalMoves[i], parameters)));
		}
		
		for (int i = 0; i<eval.length; i++) {
			Future<Double> aFuture = futureList.get(i);
			//utility function
			try {
				eval[i] = aFuture.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			if ( (i > 0) && (eval[i] > eval[maxId]) )
				maxId = i;
		}
	
		return maxId;
	}
	
	/**
	 * main function
	 */
	public void play() {

		State s = new State();
		//new TFrame(s);
		while (!s.hasLost()) {
			if (count > 1000) {
				break;
			}// for debugging
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
			
			  try { Thread.sleep(3); } catch (InterruptedException e) {
			  e.printStackTrace(); }
			 
		}
		L = s.getRowsCleared();
		System.out.println("You have cleared: " + L);
	}

	static public void main(String[] args) {
		ParallelPlayer2 p = new ParallelPlayer2();
		p.play();
	}
}
