/**
 * @deprecated fun to write but totally useless
 */
import java.util.concurrent.*;
public class UtilityFunctions extends RecursiveTask<Integer>{
	// Select which function to use
	private int selector;
	private int[][] field;
	private int[] top;
	private int pHeight;
	private int height;
	
	// Constructors for different types of functions
	public UtilityFunctions(int selector) {
		this.selector = selector;
	}
	
	public UtilityFunctions(int selector, int pHeight, int height) {
		this.selector = selector;
		this.pHeight = pHeight;
		this.height = height;
	}
	
	public UtilityFunctions(int selector, int[][] field) {
		this.selector = selector;
		this.field = field;
	}
	
	public UtilityFunctions(int selector, int[] top) {
		this.selector = selector;
		this.top = top;
	}
	
	public UtilityFunctions(int selector, int[][] field, int[] top) {
		this.selector = selector;
		this.field = field;
		this.top = top;
	}
	
	public void setField(int[][] field) {
		this.field = field;
	}

	public void setTop(int[] top) {
		this.top = top;
	}

	public void setpHeightAndHeight(int pHeight, int height) {
		this.pHeight = pHeight;
		this.height = height;
	}

	public void setFieldAndTop(int[][] field, int[] top) {
		this.field = field;
		this.top = top;
	}
	/**
	 * Get landing height of a piece [Selector = 1]
	 * 
	 * @param pHeight
	 *            : height of the piece
	 * @param height
	 *            : height of landing point
	 * @return landing height of a piece
	 */
	public int GetLandingHeight() {
		int LH = 0;

		LH = height + ((pHeight-1) / 2);
		return LH;
	}
	
	/**
	 * Get row transition number [Selector = 2]
	 * 
	 * @param field
	 *            :field array in state
	 * @return row transition number
	 */
	public int GetRowTransitions() {
		int RT = 0;
		int previous_state = 1;

		for (int row = 0; row < State.ROWS-1; row++) {    
			for (int col = 0; col < State.COLS; col++) {
				if ((field[row][col] !=0)!= (previous_state!=0)) {
					RT++;
				}
				previous_state = field[row][col];
			}
			if (field[row][State.COLS - 1] == 0)
				RT++;
			previous_state = 1;
		}
		return RT;
	}
	
	/**
	 * Get column transition number [Selector = 3]
	 * 
	 * @param field
	 *            :field array in state
	 * @return column transition number
	 */
	public int GetColumnTransitions() {
		int CT = 0;
		int previous_state = 1;

		for (int col = 0; col < State.COLS; col++) {
			for (int row = 0; row < State.ROWS-1; row++) {					
				if ((field[row][col] !=0)!= (previous_state!=0)) {
					CT++;
				}
				
				if (field[State.ROWS-1][col] == 0)                          //Li Chenhao
					CT++;
				previous_state = field[row][col];
			}
			previous_state = 1;
		}
		return CT;
	}
	
	/**
	 * Get nubmer of holes [Selector = 4]
	 * 
	 * @param top
	 *            :top array in state
	 * @param field
	 *            :field array in state
	 * @return : number of holes of given state
	 */
	public int GetNumberOfHoles() {
		int holes = 0;
		
		for (int j = 0; j < State.COLS; j++) {
			for (int i = 0; i < top[j] - 1; i++) {
				if (field[i][j] == 0)
					holes++;
			}
		}
		return holes;
	}
	
	/**
	 * Get number of wells (with depth accumulated) [Selector = 5]
	 * 
	 * @param top
	 *            :top array in state
	 * @param field
	 *            :field array in state
	 * @return : number of wells(with depth accumulated)
	 */
	public int GetWellSums() {
		int well = 0;
		for (int col = 1; col < State.COLS - 1; col++) {
			for (int row = State.ROWS - 2; row >= 0; row--) {   
				if (field[row][col] == 0 && field[row][col - 1] != 0
						&& field[row][col + 1] != 0) {
					well++;
					for (int i = row - 1; i >= 0; i--) {
						if (field[i][col] == 0) {
							well++;
						} else {
							break;
						}
					}
				}
			}
		}

		for (int row = State.ROWS - 2; row >= 0; row--) {     
			if (field[row][0] == 0 && field[row][1] != 0) {
				well++;
				for (int i = row - 1; i >= 0; i--) {
					if (field[i][0] == 0) {
						well++;
					} else {
						break;
					}
				}
			}
		}

		for (int row = State.ROWS - 2; row >= 0; row--) {    
			if (field[row][State.COLS - 1] == 0
					&& field[row][State.COLS - 2] != 0) {
				well++;
				for (int i = row - 1; i >= 0; i--) {
					if (field[i][State.COLS - 1] == 0) {
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
	 * get the max column height of the field [Selector = 6]
	 * 
	 * @param top
	 *            :top array of state
	 * @return max column height
	 */
	private int getMax() {
		int max = top[0];

		for (int i = 0; i < top.length; i++) {
			max = Math.max(max, top[i]);
		}

		return max;
	}
	
	@Override
	protected Integer compute() {
		switch(selector) {
		case 0:
		case 1:
			return GetLandingHeight();
		case 2:
			return GetRowTransitions();
		case 3:
			return GetColumnTransitions();
		case 4:
			return GetNumberOfHoles();
		case 5:
			return GetWellSums();
		case 6:
			return getMax();
		default:
			return 0;	
		}
	}
	
}
