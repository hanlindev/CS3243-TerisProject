import java.awt.Color;

public class PlayerSkeleton {

	private int getMax(int[] arr) {
		int max = arr[0];

		for (int i = 0; i < arr.length - 1; i++)
			max = Math.max(max, arr[i]);

		return max;
	}

	private double dampening_f(double c, double k) {
		return Math.pow(c, 1.0 / k);
	}

	private int getHoleNum(int[][] field, int[] tops) {
		int count = 0;
		for (int j = 0; j < field[0].length; j++) {
			for (int i = 0; i < tops[j] - 1; i++) {
				if (field[i][j] == 0)
					count++;
			}
		}
		return count;
	}
	private double multiWellPenalty(int[][] field,int[] tops){
		int count = 0;
		double cost = 0.0;
		int colNumber = field[0].length;

		for(int j=1; j<colNumber-1;j++){
			if(tops[j-1]-tops[j]>2 && tops[j+1]-tops[j]>2){
				count++;
			}
			cost += 0.44*(count-1);
		}
		return cost;
	}

	 private boolean[][] dependency(int[][] field, int[] top, int n){
		 int colNumber = field[0].length;
		 int rowNumber = field.length;
		 boolean[][] d = new boolean[rowNumber][rowNumber];
		 for(int i=0;i<rowNumber;i++)
			 for(int j=0;j<rowNumber;j++)
				 d[i][j]=false;
			
		 for(int i =n-2;i>=0;i--)
			 for(int j=0;j<colNumber;j++){
				 if(field[i][j]==0 && top[j]>i){
					 //hole
					 for(int k = i+1; k<top[j];k++){
						 if(field[k][j]!=0){//occupied
							 d[i][k] = true;
						 }else{//space
							 for(int h = k+1;h<top[j];h++){
								 if(d[k][h]==true)
									 d[i][h] = true;
							 }
						 }
					 }
				 }
			 }
		 return d;
	 }
	
	private double getHoleCost(int[][] field, int[] top, int row, boolean[] d){
		int colNumber = field[0].length;
		double holeCost = 0.0;
		
		if(row == field.length-1) return holeCost; //no hole for the topmost row
		if(row == field.length-2){
			int holeSize = 1;
			for(int j = 0;j<colNumber;){
				if(row < top[j] && field[row][j]==0){//hole open
					while(j+holeSize < colNumber && field[row][j+holeSize]==0 && row<top[j+holeSize])
						holeSize++;
					if(holeSize==1){
						if(j-1<0||j+1==colNumber) holeCost += 2.57;//tall blank/blank tall
						else if (j-1==0 || j+2==colNumber) holeCost += 1.34; //wide blank/blank wide
						else holeCost += 1.55; //blank blank
					}else if(holeSize == 2){
							if(j-1<0 || j+2==colNumber) holeCost += 1.79; //(tall blank/blank tall)
						else if(j-1 == 0 || j+3==colNumber) holeCost += 1.31; //wide blank/blank wide
						else holeCost += 1.63;//blank blank
					}else //gapSize larger than 2
						holeCost += 1.0;
				}
				holeSize = 1;
				j+=holeSize;
			}
			return holeCost;
		}
		
		int holeSize=1;
		for(int j=0;j<colNumber;){
			if(row < top[j] && field[row][j]==0){//hole open
				while(j+holeSize < colNumber && field[row][j+holeSize]==0 && row<top[j+holeSize])
					holeSize++;
				//    2   2
				//1 0   0 1
				//##   ##
				int[][] pos = {{-1,-1,-1},{-1,-1,-1}};
				if(holeSize ==1){
					//get the information of the 6 positions
					for(int k = row+1; k<top[j] && pos[0][2]!=-1;k++){
						if(!d[k] && pos[0][0]!=-1){//the first independent row 
							if(j>0) pos[0][0]=field[k][j-1];
							if(j<colNumber-1) pos[1][0]=field[k][j+1];
							if(j>1) pos[0][1]=field[k][j-2];
							if(j<colNumber-2) pos[1][1]=field[k][j+2];
							k++;
						}
						if(!d[k]){//the second independent row
							if(j>0) pos[0][2]=field[k][j-1];
							if(j<colNumber-1) pos[1][2]=field[k][j+1];
						}
					}
					for(int x=0;x<2;x++)
						for(int y=0;y<3;y++)
							if(pos[x][y]==-1)
								pos[x][y]=0;
					
				}else if(holeSize==2){
					for(int k = row+1; k<top[j] && pos[0][2]!=-1;k++){
						if(!d[k] && pos[0][0]!=-1){//the first independent row 
							if(j>0) pos[0][0]=field[k][j-1];
							if(j<colNumber-2) pos[1][0]=field[k][j+2];
							if(j>1) pos[0][1]=field[k][j-2];
							if(j<colNumber-3) pos[1][1]=field[k][j+3];
							k++;
						}
						if(!d[k]){//the second independent row
							if(j>0) pos[0][2]=field[k][j-1];
							if(j<colNumber-2) pos[1][2]=field[k][j+2];
						}
					}
					for(int x=0;x<2;x++)
						for(int y=0;y<3;y++)
							if(pos[x][y]==-1)
								pos[x][y]=0;
				}
				//compute the cost with pos[][];
				if(holeSize == 1 || holeSize ==2){
					if(j-1<0||pos[0][2]!=0){
						//tall on the left
						if(j+1==colNumber || pos[1][2]!=0) holeCost += (holeSize==1)?15.0:5.48; //tall tall
						else if(pos[1][0]!=0 && pos[1][2]==0) holeCost += holeSize==1?4.38:2.18;//tall short
						else if(j+2==colNumber || pos[1][1]!=0) holeCost += holeSize==1?2.55:2.09; //tall wide
						else holeCost += holeSize==1?2.57:1.79; //tall blank
					}else if(pos[0][0]!=0 && pos[0][2]==0){
						//short on the left
						if(j+1==colNumber || pos[1][2]!=0) holeCost += holeSize==1?4.38:2.18; //short tall
						else if(pos[1][0]!=0 && pos[1][2]==0) holeCost += holeSize==1?2.69:2.72;//short short
						else if(j+2==colNumber || pos[1][1]!=0) holeCost += holeSize==1?2.01:2.74; //short wide
						else holeCost += holeSize==1?2.01:2.15 ; //short blank
					}else if(j-1==0||pos[0][1]!=0){
						//wide on the left
						if(j+1==colNumber || pos[1][2]!=0) holeCost += holeSize==1?2.55:2.09; //wide tall
						else if(pos[1][0]!=0 && pos[1][2]==0) holeCost += holeSize==1?2.01:2.74;//wide short
						else if(j+2==colNumber || pos[1][1]!=0) holeCost += holeSize==1?1.55:1.77; //wide wide
						else holeCost += holeSize==1?1.34:1.31; //wide blank
					}else{
						//blank on the left
						if(j+1==colNumber || pos[1][2]!=0) holeCost += holeSize==1?2.57:1.79; //blank tall
						else if(pos[1][0]!=0 && pos[1][2]==0) holeCost += holeSize==1?2.01:2.15;//blank short
						else if(j+2==colNumber || pos[1][1]!=0) holeCost += holeSize==1?1.34:1.31; //blank wide
						else holeCost += holeSize==1?1.55:1.63; //blank blank
					}
				}else 
					holeCost += 1.0;
			}
			holeSize =1;
			j+=holeSize;
		}		
		return holeCost;
	}
	 
	private double getGapCost(int[][] field, int[] top, int row) {
		int colNumber = field[0].length;
		double gapCost = 0.0;
		
		if(row == field.length-1){
			//the very top row
			int gapSize = 1;
			for(int j = 0;j<colNumber;){
				if(field[row][j]==0){//gap open
					while(j+gapSize < colNumber && field[row][j+gapSize]==0 && row >= top[j+gapSize])
						gapSize++;
					if(gapSize == 1){
						//gap of size 1
						//blank on both or at boundary (tall/wide)
						if(j-1<0 || j+1==colNumber) gapCost += 2.57; //(tall blank/blank tall)
						else if(j-1 == 0 || j+2==colNumber) gapCost += 1.34;//(wide blank/blank wide)
						else gapCost += 1.55;//blank blank
						//deep well penalty
						if(row!=0 && field[row-1][j]==0)
							gapCost += 7.9;
					}else if(gapSize == 2){
						//gap of size 2
						//blank on both or at boundary 
						if(j-1<0 || j+2==colNumber) gapCost += 1.79; //(tall blank/blank tall)
						else if(j-1 == 0 || j+3==colNumber) gapCost += 1.31; //wide blank/blank wide
						else gapCost += 1.63;//blank blank
					}else //gapSize larger than 2
						gapCost += 1.0;
				}
				gapSize=1;
				j += gapSize;
			}
			return gapCost;
		}
		if(row == field.length -2){
			//special case for the second topmost row
			int gapSize = 1;
			for(int j = 0;j<colNumber;){
				if(row>=top[j]){//gap open
					while(j+gapSize < colNumber && field[row][j+gapSize]==0 && row >= top[j+gapSize])
						gapSize++;
					
					if(gapSize == 1){
						if(j-1<0){//tall on the left	
							if(field[row+1][j+gapSize]!=0) gapCost += 4.38;//tall short
							else if(field[row+1][j+gapSize]!=0) gapCost += 2.55; //tall wide
							else gapCost += 2.57; //tall blank
						}else if(field[row+1][j-1]!=0 && field[row+2][j-1]==0){//short on the left
							if(j+1==colNumber) gapCost += 4.38; //short tall
							else if(field[row+1][j+gapSize]!=0) gapCost += 2.69;//short short
							else if(j+2==colNumber || field[row+1][j+gapSize]!=0) gapCost += 2.01; //short wide
							else gapCost += 2.01 ; //short blank
						}else if(j-1==0||field[row+1][j-1]!=0){//wide on the left
							if(j+1==colNumber) gapCost += 2.55; //wide tall
							else if(field[row+1][j+gapSize]!=0) gapCost += 2.01;//wide short
							else if(j+2==colNumber || field[row+1][j+gapSize]!=0) gapCost += 1.55; //wide wide
							else gapCost += 1.34; //wide blank
						}else{//blank on the left
							if(j+1==colNumber) gapCost += 2.57; //blank tall
							else if(field[row+1][j+gapSize]!=0) gapCost += 2.01;//blank short
							else if(j+2==colNumber || field[row+1][j+gapSize]!=0) gapCost += 1.34; //blank wide
							else gapCost += 1.55; //blank blank
						}
						//deep well penalty
						if(row!=0 && field[row-1][j]==0)
							gapCost += 7.9;
					}else if(gapSize == 2){
						if(j-1<0){//tall on the left
							if(field[row+1][j+gapSize]!=0 && field[row+2][j+gapSize]==0) gapCost += 2.18;//tall short
							else if(j+3==colNumber || field[row+1][j+gapSize]!=0) gapCost += 2.09; //tall wide
							else gapCost += 1.79; //tall blank
						}else if(field[row+1][j-1]!=0 && field[row+2][j-1]==0){
							//short on the left
							if(j+2==colNumber) gapCost += 2.18; //short tall
							else if(field[row+1][j+gapSize]!=0) gapCost += 2.72;//short short
							else if(j+3==colNumber || field[row+1][j+gapSize]!=0) gapCost += 2.74; //short wide
							else gapCost += 2.15; //short blank
						}else if(j-1==0||field[row+1][j-1]!=0){//wide on the left
							if(j+2==colNumber) gapCost += 2.09; //wide tall
							else if(field[row+1][j+gapSize]!=0) gapCost += 2.74;//wide short
							else if(j+3==colNumber || field[row+1][j+gapSize]!=0) gapCost += 1.77; //wide wide
							else gapCost += 1.31; //wide blank
						}else{//blank on the left
							if(j+2==colNumber) gapCost += 1.79; //blank tall
							else if(field[row+1][j+gapSize]!=0) gapCost += 2.15;//blank short
							else if(j+3==colNumber || field[row+1][j+gapSize]!=0) gapCost += 1.31; //blank wide
							else gapCost += 1.63; //blank blank
						}
					}else //gapSize larger than 2
						gapCost += 1.0;
				}
				gapSize =1;
				j += gapSize;
			}
			return gapCost;
		}
		
		for (int j = 0; j < colNumber;) {
			int gapSize=1;

			if (row >= top[j]) {// gap open
				while (j+gapSize < colNumber && row >= top[j+gapSize])
					gapSize++;
				
				if (gapSize == 1) {
					// gap of size 1
					if(j-1<0||field[row+2][j-1]!=0){
						//tall on the left
						if(j+1==colNumber || field[row+2][j+gapSize]!=0) gapCost += 15.0; //tall tall
						else if(field[row+1][j+gapSize]!=0 && field[row+2][j+gapSize]==0) gapCost += 4.38;//tall short
						else if(j+2==colNumber || field[row+1][j+gapSize]!=0) gapCost += 2.55; //tall wide
						else gapCost += 2.57; //tall blank
					}else if(field[row+1][j-1]!=0 && field[row+2][j-1]==0){
						//short on the left
						if(j+1==colNumber || field[row+2][j+gapSize]!=0) gapCost += 4.38; //short tall
						else if(field[row+1][j+gapSize]!=0 && field[row+2][j+gapSize]==0) gapCost += 2.69;//short short
						else if(j+2==colNumber || field[row+1][j+gapSize]!=0) gapCost += 2.01; //short wide
						else gapCost += 2.01 ; //short blank
					}else if(j-1==0||field[row+1][j-1]!=0){
						//wide on the left
						if(j+1==colNumber || field[row+2][j+gapSize]!=0) gapCost += 2.55; //wide tall
						else if(field[row+1][j+gapSize]!=0 && field[row+2][j+gapSize]==0) gapCost += 2.01;//wide short
						else if(j+2==colNumber || field[row+1][j+gapSize]!=0) gapCost += 1.55; //wide wide
						else gapCost += 1.34; //wide blank
					}else{
						//blank on the left
						if(j+1==colNumber || field[row+2][j+gapSize]!=0) gapCost += 2.57; //blank tall
						else if(field[row+1][j+gapSize]!=0 && field[row+2][j+gapSize]==0) gapCost += 2.01;//blank short
						else if(j+2==colNumber || field[row+1][j+gapSize]!=0) gapCost += 1.34; //blank wide
						else gapCost += 1.55; //blank blank
					}
					//deep well penalty
					if(row!=0 && field[row-1][j]==0)
						gapCost += 7.9;
				} else if (gapSize == 2) {
					// gap of size 2
					if(j-1<0||field[row+2][j-1]!=0){
						//tall on the left
						if(j+2==colNumber || field[row+2][j+gapSize]!=0) gapCost += 5.48; //tall tall
						else if(field[row+1][j+gapSize]!=0 && field[row+2][j+gapSize]==0) gapCost += 2.18;//tall short
						else if(j+3==colNumber || field[row+1][j+gapSize]!=0) gapCost += 2.09; //tall wide
						else gapCost += 1.79; //tall blank
					}else if(field[row+1][j-1]!=0 && field[row+2][j-1]==0){
						//short on the left
						if(j+2==colNumber || field[row+2][j+gapSize]!=0) gapCost += 2.18; //short tall
						else if(field[row+1][j+gapSize]!=0 && field[row+2][j+gapSize]==0) gapCost += 2.72;//short short
						else if(j+3==colNumber || field[row+1][j+gapSize]!=0) gapCost += 2.74; //short wide
						else gapCost += 2.15; //short blank
					}else if(j-1==0||field[row+1][j-1]!=0){
						//wide on the left
						if(j+2==colNumber || field[row+2][j+gapSize]!=0) gapCost += 2.09; //wide tall
						else if(field[row+1][j+gapSize]!=0 && field[row+2][j+gapSize]==0) gapCost += 2.74;//wide short
						else if(j+3==colNumber || field[row+1][j+gapSize]!=0) gapCost += 1.77; //wide wide
						else gapCost += 1.31; //wide blank
					}else{
						//blank on the left
						if(j+2==colNumber || field[row+2][j+gapSize]!=0) gapCost += 1.79; //blank tall
						else if(field[row+1][j+gapSize]!=0 && field[row+2][j+gapSize]==0) gapCost += 2.15;//blank short
						else if(j+3==colNumber || field[row+1][j+gapSize]!=0) gapCost += 1.31; //blank wide
						else gapCost += 1.63; //blank blank
					}
				} else
					// gap of size larger than 2, uniform cost 1.0
					gapCost += 1.0;
			}
			gapSize =1;
			j += gapSize;
		}

		return gapCost;
	}

	private double utilityFunction(State s, int[] move) {

		int[] top = s.getTop();
		int[][] field = s.getField();
		int orient = move[State.ORIENT];
		int slot = move[State.SLOT];
		int nextPiece = s.getNextPiece();
		int[][] pWidth = State.getpWidth();
		int[][] pHeight = State.getpHeight();
		int[][][] pTop = State.getpTop();
		int[][][] pBottom = State.getpBottom();
		int turn = s.getTurnNumber();
		boolean lost = false;
		int rowsCleared = 0;

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
			lost = true;

		// meta_parameters
		double meta_a = -100;
		double meta_k = 4.384;
		double meta_A = 1.16;
		double meta_B = 17.72;
		double meta_epsilon = 0.344;
		
		int topMax = getMax(s.getTop());
		//dependency matrix
		boolean [][] d = new boolean[State.ROWS-1][State.ROWS-1];
		d = dependency(field,top,topMax);
		// cost c
		double[] c = new double[topMax];
		// compute c
		for (int i = topMax - 1; i >= 0; i--) {
			c[i] = meta_B * getHoleNum(field, top) + getGapCost(field, top, i) + getHoleCost(field,top,i,d[i]);
			for(int j=i+1;j<topMax;j++){
				if(d[i][j]);
					c[i]+=c[j]+meta_A;
			}
		}

		// cost function R+V
		double R;
		double V = 0.0;

		// compute V
		for (int i = 0; i < topMax; i++) {
			V += dampening_f(c[i], meta_k);
		}
		V += multiWellPenalty(field,top);

		// compute R
		if (lost)
			R = 10000000;
		else
			R = meta_a * (rowsCleared);

		return R + V;
	}

	// implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int minId = 0;
		double temp;
		double cost = utilityFunction(s, legalMoves[0]);
		// choose the one with smallest cost
		for (int i = 1; i < legalMoves.length; i++) {
			temp = utilityFunction(s, legalMoves[i]);
			if (temp < cost) {
				cost = temp;
				minId = i;
			}

		}
		return minId;
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
			s.draw();
			s.drawNext(0, 0);
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed " + s.getRowsCleared()
				+ " rows.");
	}

}

/**
 * myState
 * @author DIdiHL
 * (spiritually) extends State to make it comparable (for look ahead)
 * and to hold the action that leads to this state
 */
class myState extends State implements Comparable<myState> {
	// Previous move that led to this state
	private int previousMove;
	// The score of this state (utility)
	private int score;
	
	public static final int COLS = 10;
	public static final int ROWS = 21;
	public static final int N_PIECES = 7;

	

	public boolean lost = false;
	
	
	

	
	public TLabel label;
	
	//current turn
	private int turn = 0;
	private int cleared = 0;
	
	//each square in the grid - int means empty - other values mean the turn it was placed
	private int[][] field = new int[ROWS][COLS];
	//top row+1 of each column
	//0 means empty
	private int[] top = new int[COLS];
	
	
	//number of next piece
	protected int nextPiece;
	
	
	
	//all legal moves - first index is piece type - then a list of 2-length arrays
	protected static int[][][] legalMoves = new int[N_PIECES][][];
	
	//indices for legalMoves
	public static final int ORIENT = 0;
	public static final int SLOT = 1;
	
	//possible orientations for a given piece type
	protected static int[] pOrients = {1,2,4,4,4,2,2};
	
	//These attributes are kept here coz I don't want to modify so much
	//code in the move method
	//the next several arrays define the piece vocabulary in detail
	//width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = {
			{2},
			{1,4},
			{2,3,2,3},
			{2,3,2,3},
			{2,3,2,3},
			{3,2},
			{3,2}
	};
	//height of the pieces [piece ID][orientation]
	private static int[][] pHeight = {
			{2},
			{4,1},
			{3,2,3,2},
			{3,2,3,2},
			{3,2,3,2},
			{2,3},
			{2,3}
	};
	private static int[][][] pBottom = {
		{{0,0}},
		{{0},{0,0,0,0}},
		{{0,0},{0,1,1},{2,0},{0,0,0}},
		{{0,0},{0,0,0},{0,2},{1,1,0}},
		{{0,1},{1,0,1},{1,0},{0,0,0}},
		{{0,0,1},{1,0}},
		{{1,0,0},{0,1}}
	};
	private static int[][][] pTop = {
		{{2,2}},
		{{4},{1,1,1,1}},
		{{3,1},{2,2,2},{3,3},{1,1,2}},
		{{1,3},{2,1,1},{3,3},{2,2,2}},
		{{3,2},{2,2,2},{2,3},{1,2,1}},
		{{1,2,2},{3,2}},
		{{2,2,1},{2,3}}
	};
	
	//initialize legalMoves
	{
		//for each piece type
		for(int i = 0; i < N_PIECES; i++) {
			//figure number of legal moves
			int n = 0;
			for(int j = 0; j < pOrients[i]; j++) {
				//number of locations in this orientation
				n += COLS+1-pWidth[i][j];
			}
			//allocate space
			legalMoves[i] = new int[n][2];
			//for each orientation
			n = 0;
			for(int j = 0; j < pOrients[i]; j++) {
				//for each slot
				for(int k = 0; k < COLS+1-pWidth[i][j];k++) {
					legalMoves[i][n][ORIENT] = j;
					legalMoves[i][n][SLOT] = k;
					n++;
				}
			}
		}
	
	}
	
	
	
	
	public int[][] getField() {
		return field;
	}

	public int[] getTop() {
		return top;
	}

    public static int[] getpOrients() {
        return pOrients;
    }
    
    public static int[][] getpWidth() {
        return pWidth;
    }

    public static int[][] getpHeight() {
        return pHeight;
    }

    public static int[][][] getpBottom() {
        return pBottom;
    }

    public static int[][][] getpTop() {
        return pTop;
    }


	public int getNextPiece() {
		return nextPiece;
	}
	
	public boolean hasLost() {
		return lost;
	}
	
	public int getRowsCleared() {
		return cleared;
	}
	
	public int getTurnNumber() {
		return turn;
	}
	
	public int getPreviousMove() {
		return previousMove;
	}
	
	public int getScore() {
		return score;
	}
	
	
	//constructor
	public myState(State s) {
		copyState(s);
	}
	
	public myState(State s, int prevMove, int points) {
		copyState(s);
		previousMove = prevMove;
		score = points;
	}
	
	private int[] copy1Array(int[] arr) {
		int[] rv = new int[arr.length];
		System.arraycopy(arr, 0, rv, 0, arr.length);
		return rv;
	}
	
	private int[][] copy2Array(int[][] arr) {
		int[][] rv = new int[arr.length][];
		for (int i = 0; i < rv.length; ++i) {
			rv[i] = copy1Array(arr[i]);
		}
		return rv;
	}
	
	private int[][][] copy3Array(int[][][] arr) {
		int[][][] rv = new int[arr.length][][];
		for (int i = 0; i < rv.length; ++i) {
			rv[i] = copy2Array(arr[i]);
		}
		return rv;
	}
	private void copyState(State s) {
		nextPiece = s.getNextPiece();
		top = copy1Array(s.getTop());
		field = copy2Array(s.getField());
		lost = s.hasLost();
		cleared = s.getRowsCleared();
		turn = s.getTurnNumber();
	}

	
	//random integer, returns 0-6
	private int randomPiece() {
		return (int)(Math.random()*N_PIECES);
	}


	
	//gives legal moves for 
	public int[][] legalMoves() {
		return legalMoves[nextPiece];
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
			return false;
		}

		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}
		
		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
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
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}
	

		//pick a new piece
		nextPiece = randomPiece();
		

		
		return true;
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
	
	@Override
	public int compareTo(myState aMyState) {
		return score - aMyState.getScore();
	}
}
