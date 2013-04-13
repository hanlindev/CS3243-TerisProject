package parallelpso;
import java.util.concurrent.*;

import net.sourceforge.jswarm_pso.*;
import net.sourceforge.jswarm_pso.Particle;
import java.util.*;

public class ParallelSwarm extends Swarm{
	public ExecutorService mainPool;
	MyFitnessFunction sampleFitnessFunction;
	public ArrayList<MyFitnessFunction> particleFitnessFunction;
	// TODO add a list of fitness functions

	public ParallelSwarm(int numberOfParticles, Particle sampleParticle,
			MyFitnessFunction fitnessFunction) {
		super(numberOfParticles, sampleParticle, fitnessFunction);
		sampleFitnessFunction = fitnessFunction;
		this.particleFitnessFunction = new ArrayList<MyFitnessFunction>(numberOfParticles);
		for (int i = 0; i < numberOfParticles; ++i) {
			this.particleFitnessFunction.add(this.sampleFitnessFunction.getInstance());
		}
		super.setParticleUpdate(new ParticleUpdateRandomByParticle(sampleParticle));
	}
	
	public ParallelSwarm(int numberOfParticles, Particle sampleParticle,
			MyFitnessFunction fitnessFunction, int numProcess) {
		super(numberOfParticles, sampleParticle, fitnessFunction);
		mainPool = Executors.newFixedThreadPool(numProcess * numberOfParticles * 5);
		sampleFitnessFunction = fitnessFunction;
		this.particleFitnessFunction = new ArrayList<MyFitnessFunction>(numberOfParticles);
		for (int i = 0; i < numberOfParticles; ++i) {
			this.particleFitnessFunction.add(this.sampleFitnessFunction.getInstance());
		}
		super.setParticleUpdate(new ParticleUpdateFullyRandom(sampleParticle));
		/*
		
		*/
	}
	
	@Override
	/**
	 * added parallel calls to fitness functions
	 */
	public void evaluate() {
		Particle[] particles = super.getParticles();
		FitnessFunction fitnessFunction = super.getFitnessFunction();
		Neighborhood neighborhood = super.getNeighborhood();
		Particle sampleParticle = super.getSampleParticle();
		
		double bestFitness = super.getBestFitness();
		int bestParticleIndex = super.getBestParticleIndex();
		int numberOfEvaliations = super.getNumberOfEvaliations();
		double[] bestPosition = super.getBestPosition();
		if (particles == null) throw new RuntimeException("No particles in this swarm! May be you need to call Swarm.init() method");
		if (fitnessFunction == null) throw new RuntimeException("No fitness function in this swarm! May be you need to call Swarm.setFitnessFunction() method");

		// Initialize
		if (Double.isNaN(bestFitness)) {
			bestFitness = (fitnessFunction.isMaximize() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			bestParticleIndex = -1;
		}

		//---
		// Evaluate each particle (and find the 'best' one)
		//---
		ArrayList<Future<Double>> futureList = new ArrayList<Future<Double>>(super.getNumberOfParticles());
		for (int i = 0; i < particles.length; ++i) {
			// Set fitness function instances corresponding particles and submit to pool
			MyFitnessFunction aFunction = particleFitnessFunction.get(i);
			aFunction.setParticle(particles[i]);
			futureList.add(this.mainPool.submit(aFunction));
		}
		
		// Update fitness value
		for (int i = 0; i < particles.length; ++i) {
			try {
				double fit = futureList.get(i).get();
				
				++numberOfEvaliations;
				super.setNumberOfEvaliations(numberOfEvaliations);
				
				// Update 'best global' position
				if (fitnessFunction.isBetterThan(bestFitness, fit)) {
					bestFitness = fit;
					bestParticleIndex = i;
					if (bestPosition == null) bestPosition = new double[sampleParticle.getDimension()];
					particles[bestParticleIndex].copyPosition(bestPosition);
					super.setBestPosition(bestPosition);
				}
				
				// Update 'best neighborhood'
				if (neighborhood != null) {
					neighborhood.update(this, particles[i]);
				}
				
				System.out.println("Particle " + i + " has position: " + particles[i].toString());//for debugging
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			} catch (ExecutionException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	/*
	@Override
	public void init() {
		Particle[] particles = new Particle[getNumberOfParticles()];
		
		double[] maxPosition = super.getMaxPosition();
		double[] minPosition = super.getMinPosition();
		double[] maxVelocity = super.getMaxVelocity();
		double[] minVelocity = super.getMinVelocity();
		Particle sampleParticle = super.getSampleParticle();
		// Check constraints (they will be used to initialize particles)
		if (maxPosition == null) throw new RuntimeException("maxPosition array is null!");
		if (minPosition == null) throw new RuntimeException("maxPosition array is null!");
		if (maxVelocity == null) {
			// Default maxVelocity[]
			int dim = sampleParticle.getDimension();
			maxVelocity = new double[dim];
			for (int i = 0; i < dim; i++)
				maxVelocity[i] = (maxPosition[i] - minPosition[i]) / 2.0;
		}
		if (minVelocity == null) {
			// Default minVelocity[]
			int dim = sampleParticle.getDimension();
			minVelocity = new double[dim];
			for (int i = 0; i < dim; i++)
				minVelocity[i] = -maxVelocity[i];
		}
		
		super.setMaxVelocity(maxVelocity);
		super.setMinVelocity(minVelocity);
		

	}
	*/

}
