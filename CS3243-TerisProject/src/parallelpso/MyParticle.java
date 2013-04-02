package parallelpso;
import net.sourceforge.jswarm_pso.Particle;

public class MyParticle extends Particle{
	public static int dim = 7;
	public static double[] position = {-3.3200740, 2.70317569, -2.7157289, -5.1061407, -6.9380080, -2.4075407, -1.0};
	
	public MyParticle() {
		super(dim);
		super.setPosition(position);
		
	}
}