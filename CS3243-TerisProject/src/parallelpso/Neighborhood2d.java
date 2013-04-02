package parallelpso;
// von neumann neighbourhood, extending Neighborhood class from
// jswarm package
import java.util.*;

import net.sourceforge.jswarm_pso.*;
import net.sourceforge.jswarm_pso.Particle;
public class Neighborhood2d extends Neighborhood{
	private class Coordinates {
		public int x, y;
		public Coordinates(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof Coordinates)) {
				return false;
			} else {
				Coordinates another = (Coordinates) o;
				return x == another.x && y == another.y;
			}
		}
		
		public int hashCode() {
			return x * 100 + y;
		}
	}
	Particle[][] grid;
	int dim;
	HashMap<Particle, Coordinates> setCoordinates;
	
	public Neighborhood2d(Swarm swarm) {
		setCoordinates = new HashMap<Particle, Coordinates>(swarm.getNumberOfParticles());
		dim = findDimensions(swarm.getNumberOfParticles());
		grid = new Particle[dim][dim];
		init(swarm);
	}
	
	private int findDimensions(int num) {
		return (int) Math.sqrt((double) num);
	}
	
	@Override
	public void init(Swarm swarm) {
		int i = 0, j = 0;
		for (Particle p: swarm) {
			grid[i][j] = p;
			setCoordinates.put(p, new Coordinates(i, j));
			if (++j == dim) {
				++i;
				j = 0;
			}
		}
	}
	
	@Override
	public Collection<Particle> calcNeighbours(Particle p) {
		ArrayList<Particle> rv = new ArrayList<Particle>();
		Coordinates coor = setCoordinates.get(p);
		int x = coor.x, y = coor.y;
		int neix, neiy;
		
		// Up neighbour
		neix = x - 1;
		neix = (neix < 0) ? dim - 1: neix;
		neiy = y;
		rv.add(grid[neix][neiy]);
		
		// Down neighbour
		neix = x + 1;
		neix = (neix == dim) ? 0 : neix;
		neiy = y;
		rv.add(grid[neix][neiy]);
		
		// Left neighbour
		neix = x;
		neiy = (y - 1 < 0) ? (dim - 1) : (y - 1);
		rv.add(grid[neix][neiy]);
		
		// Right neighbour
		neix = x;
		neiy = (y + 1 == dim) ? 0 : y + 1;
		rv.add(grid[neix][neiy]);
		
		return rv;
	}
	
}
