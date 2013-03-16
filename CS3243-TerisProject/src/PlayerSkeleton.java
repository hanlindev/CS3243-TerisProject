
public class PlayerSkeleton {

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		
		return 0;
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}

/**
 * myState
 * @author DIdiHL
 * (spiritually) extends State to make it comparable (for look ahead)
 * and to hold the action that leads to this state
 */
class myState extends State implements Comparable<myState> {
	private int previousPiece;
	private int[] top;
	private int[][] filed;
	private int orient;
	private int slot;
	private int nextPiece;
	private int[][] pWidth;
	private int[][] pHeight;
	private int[][][] pTop;
	private int[][][] pBottom;
	private int turn;
	
	public myState(State s) {
		copyState(s);
	}
	
	public myState(myState s) {
		copyMyState(s);
	}
	
	public myState(myState s, int previousPiece) {
		
	}
	
}