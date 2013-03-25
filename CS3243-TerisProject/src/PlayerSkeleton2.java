import java.util.PriorityQueue;
import java.io.*;

public class PlayerSkeleton2 {

	// implement this function to have a working system
	public int pickMoveLookAhead(State s, int[][] legalMoves) {
		double[] eval = new double[legalMoves.length];
		int maxId = 0;
		MyState ms = new MyState(s);
		
		
		for (int i = 0; i<eval.length; i++) {
			//reward
			
			//utility function
			//eval[i] = utilityFunction_v2(s, legalMoves[i]);
			ms.makeMove(i);
			if (!ms.hasLost()) {
				eval[i] = lookAhead(ms, 1);
			}
			ms.undo();
			//System.out.println(eval[i]);
			
			if ( (i > 0) && (eval[i] > eval[maxId]) )
				maxId = i;
		}
		return maxId;
	}
	
	public int pickMove(State s, int[][] legalMoves) {
		double[] eval = new double[legalMoves.length];
		int maxId = 0;
		
		MyState ms = new MyState(s);
		for (int i = 0; i<eval.length; i++) {
			//reward
			//myState ms = new myState(s);
			//utility function
			eval[i] = utilityFunction(ms, legalMoves[i]);
			//System.out.println(eval[i]);
			
			if ( (i > 0) && (eval[i] > eval[maxId]) )
				maxId = i;
		}
		return maxId;
	}

	public double utilityFunction_v2(State s, int[] move) {


		// simulate make move
		int[] top = s.getTop();
		int[][] field = s.getField();
		int completed = s.getRowsCleared();
		int orient = move[State.ORIENT];
		int slot = move[State.SLOT];
		int nextPiece = s.getNextPiece();
		int[][] pWidth = State.getpWidth();
		int[][] pHeight = State.getpHeight();
		int[][][] pTop = State.getpTop();
		int[][][] pBottom = State.getpBottom();
		int turn = s.getTurnNumber() + 1;
		int[] topTemp = new int[top.length];
		int[][] fieldTemp = new int[field.length][field[0].length];

		for (int i = 0; i < top.length; i++)
			topTemp[i] = top[i];

		for (int i = 0; i < field.length; i++)
			for (int j = 0; j < field[0].length; j++)
				fieldTemp[i][j] = field[i][j];

		top = topTemp;
		field = fieldTemp;

		// height if the first column makes contact
		int height = top[slot] - pBottom[nextPiece][orient][0];

		// for each column beyond the first in the piece
		for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
			height = Math.max(height, top[slot + c]
					- pBottom[nextPiece][orient][c]);
		}
		int rowsCleared = 0;

		// check if game ended
		if (height + pHeight[nextPiece][orient] < State.ROWS) {

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
		}else return -0xffff;

		double result =
				GetLandingHeight(pHeight[nextPiece][orient], height) * -3.3200740
				+ rowsCleared * 2.70317569 + GetRowTransitions(field)
				* -2.7157289 + GetColumnTransitions(field)
				* -5.1061407 + GetNumberOfHoles(top, field)
				* -6.9380080 + GetWellSums(top, field)
				* -2.4075407;
		return result;
	}
	
	public double utilityFunction(MyState s, int[] move) {
		if(s.hasLost()) {
			return Double.MAX_VALUE;
		} else {
			// Get average landing height
			int orient = move[MyState.ORIENT];
			int slot = move[MyState.SLOT];
			int nextPiece = s.getNextPiece();
			int[][] pHeight = State.getpHeight();
			int[][][] pBottom = State.getpBottom();
			int[] top = s.getTop();
			int[][] pWidth = State.getpWidth();
			
			// height if the first column makes contact
			int height = top[slot] - pBottom[nextPiece][orient][0];

			// for each column beyond the first in the piece
			for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
				height = Math.max(height, top[slot + c]
						- pBottom[nextPiece][orient][c]);
			}
			
			int rowsCleared = s.getJustCleared();
			
			int[][] field = s.getField();
			
			double result = s.getLandingHeight() * -3.3200740 
					+ rowsCleared * 2.70317569 + GetRowTransitions(field)
					* -2.7157289 + GetColumnTransitions(field)
					* -5.1061407 + GetNumberOfHoles(top, field)
					* -6.9380080 + GetWellSums(top, field)
					* -2.4075407;
			return result;
		}
	}
	
	/**
	 * lookAhead
	 * @param s the current state 
	 * @param step the remaining steps we need to look ahead. E.g.
	 *        if we want to look ahead 1 step, it should be 1.
	 * @return the best utility we can achieve from this state
	 */
	private double lookAhead(MyState s, int step) {
		if (step > 0) {
			PriorityQueue<PMSTriplet> pq = new PriorityQueue<PMSTriplet>();
			for (int i = 0; i < MyState.N_PIECES; ++i) {
				// Pick one piece and set it as the next piece
				s.setNextPiece(i);
				// Get the legal moves
				int[][] legalMoves = s.legalMoves();
				// Iterate through the legal moves and push them into the pq
				for (int j = 0; j < legalMoves.length; ++j) {
					s.makeMove(j);
					double score = utilityFunction(s, legalMoves[j]);
					pq.offer(new PMSTriplet(i, j, score));
					s.undo();
				}
			}
			
			double totalScore = 0D;
			int numExpansion = 5;
			// Choose the best 5 states to expand
			for (int i = 0; i < numExpansion; ++i) {
				PMSTriplet aGoodState = pq.poll();
				s.setNextPiece(aGoodState.piece);
				s.makeMove(aGoodState.move);
				totalScore += lookAhead(s, step - 1);
				s.undo();
			}
			
			return totalScore / (double) numExpansion;
		} else {
			double totalScore = 0D;
			int numEvaluation = 0;
			for (int i = 0; i < MyState.N_PIECES; ++i) {
				s.setNextPiece(i);
				int[][] legalMoves = s.legalMoves();
				for (int j = 0; j < legalMoves.length; ++j) {
					s.makeMove(j);
					//System.out.println("WTF2 " + s.getNextPiece());//for debugging
					totalScore += utilityFunction(s, legalMoves[j]);
					s.undo();
					//System.out.println("WTF3 " + s.getNextPiece());//for debugging
					++numEvaluation;
				}
			}
			return totalScore / (double) numEvaluation;
		}
	}
	

	public int GetLandingHeight(int pHeight, int height) {
		int LH = 0;

		LH = height + ((pHeight-1) / 2);
		return LH;
	}

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

	public int GetColumnTransitions(int[][] board) {
		int CT = 0;
		int previous_state = 1;

		for (int col = 0; col < State.COLS; col++) {
			for (int row = 0; row < State.ROWS-1; row++) {
				if ((board[row][col] !=0)!= (previous_state!=0)) {
					CT++;
				}
				
				if (board[State.ROWS-2][col] == 0)
					CT++;
				previous_state = board[row][col];
			}
			previous_state = 1;
		}
		return CT;
	}

	public int GetNumberOfHoles(int[] top, int[][] board) {
		int holes = 0;
		/*int row_holes = 0x0000;
		
		int previous_row = ArrayToBitmap(board[State.ROWS-2]);
		System.out.println("previous_row "+previous_row);
		for (int row = State.ROWS-3;row>=0;row--){
			row_holes = ArrayToNegBitmap(board[row])&(previous_row|row_holes);
			System.out.println("row_holes : "+row_holes);
			for(int col=0;col<State.COLS;col++){
				holes += ((row_holes>>=1)&1);
			}
			System.out.println("holes : "+holes);
		}*/
		for (int j = 0; j < State.COLS; j++) {
			for (int i = 0; i < top[j] - 1; i++) {
				if (board[i][j] == 0)
					holes++;
			}
		}
		return holes;
	}
	

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

	public static void main(String[] args) throws Exception {
		int rounds = 50;
		int score = 0;
		for (int i = 0; i < 50; ++i) {
			int result = testNormal(i);
			score += result;
			System.out.println("Average result: " + result);
		}
		
		System.out.println("Player 2 Total Average result: " + (score / rounds));
	}
	
	public static int testNormal(int no) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter("Player2Normal" + no + ".txt"));
		// Average Result 64005
		int rounds = 10;
		int totalCleared = 0;
		for (int i = 0; i < rounds; ++i) {
			State s = new State();
			//new TFrame(s);
			PlayerSkeleton2 p = new PlayerSkeleton2();
			int rowsCleared = 0;
			while (!s.hasLost()) {
				s.makeMove(p.pickMove(s, s.legalMoves()));
				//s.draw();
				//s.drawNext(0, 0);
				rowsCleared = s.getRowsCleared();
				if (rowsCleared % 1000 == 0) {
					System.out.println(rowsCleared);
				}
				/*try {
					Thread.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
			}
			bw.write(s.getRowsCleared() + "\n");
			bw.flush();
			totalCleared += s.getRowsCleared();
		}
		bw.write("Average score: " + totalCleared / rounds + "\n");
		bw.flush();
		return totalCleared / rounds;
	}
	
	public static int testLookAhead() throws Exception{
		BufferedWriter bw = new BufferedWriter(new FileWriter("Player2lookahead.txt"));
		int rounds = 50;
		int totalCleared = 0;
		for (int i = 0; i < rounds; ++i) {
			State s = new State();
			new TFrame(s);
			PlayerSkeleton2 p = new PlayerSkeleton2();
			int rowsCleared = 0;
			while (!s.hasLost()) {
				s.makeMove(p.pickMoveLookAhead(s, s.legalMoves()));
				s.draw();
				s.drawNext(0, 0);
				rowsCleared = s.getRowsCleared();
				if (rowsCleared % 1000 == 0) {
					System.out.println(rowsCleared);
				}
				//System.out.println(rowsCleared);
				/*try {
					Thread.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
			}
			bw.write(s.getRowsCleared() + "\n");
			totalCleared += s.getRowsCleared();
		}
		bw.write("Average Score: " + totalCleared / rounds);
		bw.flush();
		return totalCleared / rounds;
	}

}
