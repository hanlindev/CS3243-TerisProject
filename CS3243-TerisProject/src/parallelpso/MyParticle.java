package parallelpso;
import net.sourceforge.jswarm_pso.Particle;

public class MyParticle extends Particle{
	public static int dim = 7;
	public int index;
	public static double[] position = {-3.3200740, 2.70317569, -2.7157289, -5.1061407, -6.9380080, -2.4075407, -1.0};
	
	public MyParticle() {
		super(dim);
		double[] aPosition = new double[dim];
		System.arraycopy(position, 0, aPosition, 0, dim);
		super.setPosition(aPosition);
	}
	
	public MyParticle(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		double[] position = super.getPosition();
		String rv = "";
		String comma = "";
		for (double corr : position) {
			rv += comma + corr;
			comma = ", ";
		}
		return "[" + rv + "]";
	}
	
	@Override
	public Particle selfFactory() {
		return new MyParticle();
	}
}