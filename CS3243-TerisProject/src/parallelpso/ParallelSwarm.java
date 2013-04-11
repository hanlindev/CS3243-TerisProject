package parallelpso;
import java.util.concurrent.*;

import net.sourceforge.jswarm_pso.*;
import net.sourceforge.jswarm_pso.Particle;
import java.util.*;

public class ParallelSwarm extends Swarm{
	ForkJoinPool mainPool;
	MyFitnessFunction sampleFitnessFunction;
	ArrayList<MyFitnessFunction> particleFitnessFunction;
	// TODO add a list of fitness functions

	public ParallelSwarm(int numberOfParticles, Particle sampleParticle,
			MyFitnessFunction fitnessFunction) {
		super(numberOfParticles, sampleParticle, fitnessFunction);
		sampleFitnessFunction = fitnessFunction;
		this.particleFitnessFunction = new ArrayList<MyFitnessFunction>(numberOfParticles);
		for (int i = 0; i < numberOfParticles; ++i) {
			this.particleFitnessFunction.add(this.sampleFitnessFunction.getInstance());
		}
	}
	
	public ParallelSwarm(int numberOfParticles, Particle sampleParticle,
			MyFitnessFunction fitnessFunction, int numProcess) {
		super(numberOfParticles, sampleParticle, fitnessFunction);
		mainPool = new ForkJoinPool(numProcess * numberOfParticles * 5);
		sampleFitnessFunction = fitnessFunction;
		this.particleFitnessFunction = new ArrayList<MyFitnessFunction>(numberOfParticles);
		for (int i = 0; i < numberOfParticles; ++i) {
			this.particleFitnessFunction.add(this.sampleFitnessFunction.getInstance());
		}
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
		// Wait for all fitness functions to compelte
		try {
			// This is nearly equivalent to running for indefinite period of time
			this.mainPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Update fitness value
		for (int i = 0; i < particles.length; ++i) {
			try {
				double fit = futureList.get(i).get();
				
				// Update 'best global' position
				if (fitnessFunction.isBetterThan(bestFitness, fit)) {
					bestFitness = fit;
					bestParticleIndex = i;
					if (bestPosition == null) bestPosition = new double[sampleParticle.getDimension()];
					particles[bestParticleIndex].copyPosition(bestPosition);
				}
				
				// Update 'best neighborhood'
				if (neighborhood != null) {
					neighborhood.update(this, particles[i]);
				}
				
				System.out.println("Particle " + i + " has position: " + particles[i].toString());//for debugging
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				return;
			}
		}
	}

}
