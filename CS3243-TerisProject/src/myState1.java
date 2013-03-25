import java.awt.Color;
import java.util.Stack;

public class myState1 extends State {
	// Records the rounds of changes. They can be used to
	// undo changes
	Stack<Stack<Triplet>> roundsOfChangesField;
	Stack<Stack<Pair>> roundsOfChangesTop;
	Stack<Triplet> roundsOfChangesTurn;
	//current turn
	private int justCleared = 0;
	private int turn = 0;
	private int cleared = 0;
	
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
		
	public myState1(State s) {
		turn = s.getTurnNumber();
		cleared = s.getRowsCleared();
		field = s.getField();
		top = s.getTop();
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
			System.out.println("MIIIIIIIIIIIPAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");//for debugging
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
}
