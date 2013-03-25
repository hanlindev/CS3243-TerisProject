import java.util.Arrays;


public class Tools {
	public static int[] copy1Array(int[] arr) {
		int[] rv = new int[arr.length];
		System.arraycopy(arr, 0, rv, 0, arr.length);
		return rv;
	}
	
	public static int[][] copy2Array(int[][] arr) {
		int[][] rv = new int[arr.length][];
		for (int i = 0; i < rv.length; ++i) {
			rv[i] = copy1Array(arr[i]);
		}
		return rv;
	}
	
	public static int[][][] copy3Array(int[][][] arr) {
		int[][][] rv = new int[arr.length][][];
		for (int i = 0; i < rv.length; ++i) {
			rv[i] = copy2Array(arr[i]);
		}
		return rv;
	}
	static public boolean compare1Arrays(int[] a1, int[] a2) {
		return Arrays.equals(a1, a2);
	}
	
	static public boolean compare2Arrays(int[][] a1, int[][] a2) {
		if (a1.length != a2.length)
			return false;
		else {
			for (int i = 0; i < a1.length; ++i) {
				boolean temp = compare1Arrays(a1[i], a2[i]);
				if (!temp) {
					return false;
				}
			}
		}
		return true;
	}
	
	static public boolean compareStates(MyState ms, State s) {
		int[][] msField = ms.getField(), sField = s.getField();
		int[] msTop = ms.getTop(), sTop = s.getTop();
		int msNext = ms.getNextPiece(), sNext = s.getNextPiece();
		int msTurn = ms.getTurnNumber(), sTurn = s.getTurnNumber();
		boolean rv = true;
		if (!compare2Arrays(msField, sField)) {
			return false;
		}
		if (!compare1Arrays(msTop, sTop)) {
			return false;
		}
		if (msNext != sNext) {
			return false;
		}
		if (msTurn != sTurn) {
			return false;
		}
		return true;
	}
	
	static public boolean compareInstances(StateInstance ms, StateInstance s) {
		if (!compare2Arrays(ms.field, s.field) || !compare1Arrays(ms.top, s.top) || ms.next != s.next || ms.turn != s.turn) {
			return false;
		} else {
			return true;
		}
	
	}
}

class StateInstance {
	public int[][] field;
	public int[] top;
	public int next;
	public int turn;
	
	public StateInstance(State s) {
		field = Tools.copy2Array(s.getField());
		top = Tools.copy1Array(s.getTop());
		next = s.getNextPiece();
		turn = s.getTurnNumber();
	}
	
	public StateInstance(myStateOld ms) {
		field = Tools.copy2Array(ms.getField());
		top = Tools.copy1Array(ms.getTop());
		next = ms.getNextPiece();
		turn = ms.getTurnNumber();
	}
}
