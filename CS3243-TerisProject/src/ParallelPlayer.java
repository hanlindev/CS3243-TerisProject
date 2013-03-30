/**
 * @deprecated Not working at all!
 */
import java.util.concurrent.*;

public class ParallelPlayer {
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
	
	/*
	 * Asynchronous utility component calculators
	 */
	private UtilityFunctions calcLandingHeight = new UtilityFunctions(1);
	private UtilityFunctions calcRowTransitions = new UtilityFunctions(2);
	private UtilityFunctions calcColumnTransitions = new UtilityFunctions(3);
	private UtilityFunctions calcNumberOfHoles = new UtilityFunctions(4);
	private UtilityFunctions calcWellSums = new UtilityFunctions(5);
	private UtilityFunctions calcPileHeight = new UtilityFunctions(6);
	private ForkJoinPool mainPool = new ForkJoinPool(6);
	
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
	public ParallelPlayer(double LH, double RowClear, double RT, double CT,
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
	 * 
	 * Pass in an array of doubles and assign to weights
	 * accordingly
	 */
	public ParallelPlayer(double[] parameters) {
		landheight = parameters[0];
		rowclear = parameters[1];
		rowtransition = parameters[2];
		columntransition = parameters[3];
		holenumber = parameters[4];
		wellnumber = parameters[5];
		pileheight = parameters[6];
	}

	/**
	 * Constructor
	 */
	public ParallelPlayer() {// initial weight
		landheight 			= -3.3200740;
		rowclear 			= 2.70317569;
		rowtransition 		= -2.7157289;
		columntransition 	= -5.1061407;
		holenumber 			= -6.9380080;
		wellnumber 			= -2.4075407;
		pileheight 			= -1.0;// feature added
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
			eval[i] = utilityFunction(s, legalMoves[i]);
			
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
	public double utilityFunction(State s, int[] move) {
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

		// Update asynchronous objects information
		calcLandingHeight.setpHeightAndHeight(pHeight[nextPiece][orient], height);
		calcRowTransitions.setField(field);
		calcColumnTransitions.setField(field);
		calcNumberOfHoles.setFieldAndTop(fieldTemp, top);
		calcWellSums.setFieldAndTop(field, top);
		calcPileHeight.setTop(top);
		
		
		// Start running asynchronous objects
		mainPool.submit(calcLandingHeight);
		mainPool.submit(calcRowTransitions);
		mainPool.submit(calcColumnTransitions);
		mainPool.submit(calcNumberOfHoles);
		mainPool.submit(calcWellSums);
		mainPool.submit(calcPileHeight);
		try {
			mainPool.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(calcRowTransitions.isDone());//for debugging
		// Join forked processes
		double result = landheight * calcLandingHeight.join() +
				rowclear * rowsCleared +
				rowtransition * calcRowTransitions.join() +
				columntransition * calcColumnTransitions.join() +
				holenumber * calcNumberOfHoles.join() +
				wellnumber * calcWellSums.join() +
				pileheight * calcPileHeight.join();
		return result;
	}
	/**
	 * main function
	 */
	public void play() {

		State s = new State();
		new TFrame(s);
		while (!s.hasLost()) {
			s.makeMove(pickMove(s, s.legalMoves()));
			/*
			 * Four Parameter: useful for PSO
			 */
			// Prepare asynchronous calls
			calcPileHeight.setTop(s.getTop());
			calcNumberOfHoles.setFieldAndTop(s.getField(), s.getTop());
			calcRowTransitions.setField(s.getField());
			calcColumnTransitions.setField(s.getField());
			
			// Start processes
			mainPool.submit(calcPileHeight);
			mainPool.submit(calcNumberOfHoles);
			mainPool.submit(calcRowTransitions);
			mainPool.submit(calcColumnTransitions);
			try {
				mainPool.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			// Join
			int pileHeight = calcPileHeight.join();
			int numHoles = calcNumberOfHoles.join();
			int rowTransit = calcRowTransitions.join();
			int colTransit = calcColumnTransitions.join();
			
			
			Pmax = Math.max(Pmax, pileHeight);
			Psum += pileHeight;
			Hmax = Math.max(Hmax, numHoles);
			Hsum += numHoles;
			Rmax = Math.max(Rmax, rowTransit);
			Rsum += rowTransit;
			Cmax = Math.max(Cmax, colTransit);
			Csum += colTransit;
			count++;

			 s.draw();
			 s.drawNext(0, 0);
			
			  try { Thread.sleep(300); } catch (InterruptedException e) {
			  e.printStackTrace(); }
			 
		}
		L = s.getRowsCleared();
		System.out.println("You have cleared: " + L);
	}
	
	static public void main(String[] args) {
		ParallelPlayer p = new ParallelPlayer();
		p.play();
	}
}

