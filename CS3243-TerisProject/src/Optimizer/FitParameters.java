package Optimizer;


public class FitParameters {
	public double L = 0;
	public double Pmax = 0;
	public double Psum = 0;
	public double Pavg = 0;
	public double Hmax = 0;
	public double Hsum = 0;
	public double Havg = 0;
	public double Rmax = 0;
	public double Rsum = 0;
	public double Ravg = 0;
	public double Cmax = 0;
	public double Csum = 0;
	public double Cavg = 0;
	public int count = 0;
	public FitParameters(){}
	public FitParameters(int l, int pmax, int psum, int hmax, int hsum,
			int rmax, int rsum, int cmax, int csum, int count) {
		super();
		L = l;
		Pmax = pmax;
		Psum = psum;
		Hmax = hmax;
		Hsum = hsum;
		Rmax = rmax;
		Rsum = rsum;
		Cmax = cmax;
		Csum = csum;
		this.count = count;
		Pavg = Psum / count;
		Havg = Hsum / count;
		Ravg = Rsum / count;
		Cavg = Csum / count;
	}
}