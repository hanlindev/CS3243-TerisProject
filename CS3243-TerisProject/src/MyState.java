import java.awt.Color;
import java.util.Stack;

public class MyState extends State {
	// Records the rounds of changes. They can be used to
	// undo changes
	Stack<Stack<Triplet>> roundsOfChangesField;
	Stack<Stack<Pair>> roundsOfChangesTop;
	Stack<Triplet> roundsOfChangesTurn;
	//current turn
	private int justCleared = 0;
	private int turn = 0;
	private int cleared = 0;
	private int landingHeight = 0;// Used in Xiangqun's utilityFunction not working atm
	
	//each square in the grid - int means empty - other values mean the turn it was placed
	private int[][] field = new int[ROWS][COLS];
	//top row+1 of each column
	//0 means empty
	private int[] top = new int[COLS];
	//height of the pieces [piece ID][orientation]
	private static int[][] pHeight;
	private static int[][][] pBottom;
	private static int[][][] pTop;
	
	// Initialize private static arrays
	{
		pHeight = super.getpHeight();
		pBottom = super.getpBottom();
		pTop = super.getpTop();
	}

	public int getNextPiece() {
		return nextPiece;
	}
	
	public void setNextPiece(int next) {
		nextPiece = next;
	}
	
	public int getJustCleared() {
		return justCleared;
	}
	
	// Reset rounds of changes
	public void reset() {
		roundsOfChangesField = new Stack<Stack<Triplet>>();
		roundsOfChangesTop = new Stack<Stack<Pair>>();
		roundsOfChangesTurn = new Stack<Triplet>();
	}
	
	public int[][] getField() {
		return field;
	}
	
	public int[] getTop() {
		return top;
	}
	
	public int getRowsCleared() {
		return cleared;
	}
	
	public int getTurnNumber() {
		return turn;
	}
	
	public int getLandingHeight() {
		return landingHeight;
	}
	
	public MyState(State s) {
		nextPiece = s.getNextPiece();
		label = s.label;
		top = s.getTop();
		field = s.getField();
		lost = s.hasLost();
		cleared = s.getRowsCleared();
		turn = s.getTurnNumber();
		reset();
	}
	//make a move based on the move index - its order in the legalMoves list
	public void makeMove(int move) {
		makeMove(legalMoves[nextPiece][move]);
	}
	
	//make a move based on an array of orient and slot
	public void makeMove(int[] move) {
		makeMove(move[ORIENT],move[SLOT]);
	}
	
	//returns false if you lose - true otherwise
	public boolean makeMove(int orient, int slot) {
		Stack<Triplet> fieldChanges = new Stack<Triplet>();
		Stack<Pair> topChanges = new Stack<Pair>();
		Triplet turnInfo = new Triplet(turn, nextPiece, cleared);
		turn++;
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}
		
		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			lost = true;
			roundsOfChangesField.push(fieldChanges);
			roundsOfChangesTop.push(topChanges);
			roundsOfChangesTurn.push(turnInfo);
			return false;
		}

		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				fieldChanges.push(new Triplet(h, i+slot, field[h][i+slot]));
				field[h][i+slot] = turn;
			}
		}
		
		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			topChanges.push(new Pair(slot+c, top[slot+c]));
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}
		
		int rowsCleared = 0;
		
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				cleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						fieldChanges.push(new Triplet(i, c, field[i][c]));
						fieldChanges.push(new Triplet(i+1, c, field[i+1][c]));
						field[i][c] = field[i+1][c];
					}
					//lower the top
					topChanges.push(new Pair(c, top[c]));
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}
	

		//pick a new piece
		//nextPiece = randomPiece();
		justCleared = rowsCleared;
		roundsOfChangesField.push(fieldChanges);
		roundsOfChangesTop.push(topChanges);
		roundsOfChangesTurn.push(turnInfo);
		
		landingHeight = height + ((pHeight[nextPiece][orient] -1) / 2); 
		return true;
	}
	
	// Undo the last round of change taken from the stack if there is nothin
	// in the stack, return false. Otherwise return true;
	public boolean undo() {
		if (!roundsOfChangesField.isEmpty()) {
			Stack<Triplet> fieldChanges = roundsOfChangesField.pop();
			Stack<Pair> topChanges = roundsOfChangesTop.pop();
			Triplet turnInfo = roundsOfChangesTurn.pop();
			while (!fieldChanges.isEmpty()) {
				Triplet F = fieldChanges.pop();
				field[F.x][F.y] = F.turn;
			}
			while (!topChanges.isEmpty()) {
				Pair T = topChanges.pop();
				top[T.x] = T.height;
			}
			turn = turnInfo.x;
			nextPiece = turnInfo.y;
			cleared = turnInfo.turn;
			return true;
		} else {
			return false;
		}
	}
	
	public void draw() {
		label.clear();
		label.setPenRadius();
		//outline board
		label.line(0, 0, 0, ROWS+5);
		label.line(COLS, 0, COLS, ROWS+5);
		label.line(0, 0, COLS, 0);
		label.line(0, ROWS-1, COLS, ROWS-1);
		
		//show bricks
				
		for(int c = 0; c < COLS; c++) {
			for(int r = 0; r < top[c]; r++) {
				if(field[r][c] != 0) {
					drawBrick(c,r);
				}
			}
		}
		
		for(int i = 0; i < COLS; i++) {
			label.setPenColor(Color.red);
			label.line(i, top[i], i+1, top[i]);
			label.setPenColor();
		}
		
		label.show();
		
		
	}
	
	public static final Color brickCol = Color.gray; 
	
	private void drawBrick(int c, int r) {
		label.filledRectangleLL(c, r, 1, 1, brickCol);
		label.rectangleLL(c, r, 1, 1);
	}
	
	public void drawNext(int slot, int orient) {
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			for(int j = pBottom[nextPiece][orient][i]; j <pTop[nextPiece][orient][i]; j++) {
				drawBrick(i+slot, j+ROWS+1);
			}
		}
		label.show();
	}
	
	//visualization
	//clears the area where the next piece is shown (top)
	public void clearNext() {
		label.filledRectangleLL(0, ROWS+.9, COLS, 4.2, TLabel.DEFAULT_CLEAR_COLOR);
		label.line(0, 0, 0, ROWS+5);
		label.line(COLS, 0, COLS, ROWS+5);
	}
}

/*
 * When used to record turns, x is turn, y is nextPiece, turn is rowsCleared
 */
class Triplet {
	public int x, y, turn;
	public Triplet(int x, int y, int turn) {
		this.x = x;
		this.y = y;
		this.turn = turn;
	}
	
	public String toString() {
		return "(" + this.x + "," + this.y + "," + this.turn + ")";
	}
}


class Pair {
	public int x, height;
	public Pair(int x, int height) {
		this.x = x;
		this.height = height;
	}
	public String toString() {
		return "(" + this.x + "," + this.height + ")";
	}
}

/*
 * PMSTriplet means piece-move-score triplet :P
 */
class PMSTriplet implements Comparable<PMSTriplet> {
	public int piece, move;
	public double score;
	
	public PMSTriplet(int p, int m, double s) {
		piece = p;
		move = m;
		score = s;
	}
	
	public int compareTo(PMSTriplet another) {
		double diff = score - another.score;
		if (diff < 0D) {
			return -1;
		} else if (diff > 0D) {
			return 1;
		} else 
			return 0;
	}
	
	public String toString() {
		return "(" + this.piece + "," + this.move + "," + this.score + ")";
	}
}
