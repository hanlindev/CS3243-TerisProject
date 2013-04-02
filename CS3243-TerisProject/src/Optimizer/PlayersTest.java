package Optimizer;
import org.junit.Test;

import parallelpso.MyFitnessFunction;
public class PlayersTest {
	static double[] parameters = {-3.3200740, 2.70317569, -2.7157289, -5.1061407, -6.9380080, -2.4075407, -1.0};
	
	public void TestParallel() {
		for (int i = 0; i < 5; ++i) {
			ParallelPlayer2 para = new ParallelPlayer2();
			para.play();
		}
	}
	public void TestNormal() {
		PlayerSkeletonUltimate player = new PlayerSkeletonUltimate();
		player.play();
	}
	
	public void TestPara() {
		MyFitnessFunction player = new MyFitnessFunction(110);
		System.out.println("Fitness: " + player.evaluate(parameters));
	}
	
	static public void main(String[] args) {
		MyFitnessFunction player = new MyFitnessFunction(64);
		System.out.println("Fitness: " + player.evaluate(parameters));
	}
}
