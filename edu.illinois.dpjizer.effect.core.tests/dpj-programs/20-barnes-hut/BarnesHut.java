/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
/**
 * Driver class for the Barnes Hut n-body simulation
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.concurrent.CyclicBarrier;

public class BarnesHut {

	/**
	 * Number of bodies in the simulation
	 */
	private final int nbody;

	/**
	 * The geometric tree representation of the bodies
	 */
	private final Tree tree = new Tree();

	/**
	 * Constructor
	 */
	public BarnesHut(int nbody) {
		this.nbody  = nbody;
		this.tree.N = 1;
	}

	/**
	 * Constructor
	 * @param nbody Number of bodies
	 * @param nproc Number of threads
	 * @param flag Print debug info
	 */
	public BarnesHut(int nbody, int nproc, boolean flag) {
		this.nbody  = nbody;
		this.tree.N = nproc;
		this.tree.printBodies = flag;
	}

	/** print usage */
	static void printUsage() {
		System.out.print("Usage:\n");
		System.out.print("./barnes <NBODY> [N] [printOutput]\n");
		System.out.print("    where,\n");
		System.out.print("          NBODY is the number of bodies to be simulated\n");
		System.out.print("          N is the number of processors/threads\n");
		System.out.print("          third argument is for testing purposes, enter any argument to emit output, say, \"true\"\n");
		System.exit(1);
	}

	/**
	 * Program main method
	 */
	public static void main(String[] args) throws Exception {
		// Deal with args
		int nbody = 100000;
		int nproc = 1;
		boolean emitBodies = false;
		if(args.length < 1 || args.length > 3)
			printUsage();
		if(args.length == 1)
			nbody = Integer.parseInt(args[0]);
		if(args.length == 2)
		{
			nbody = Integer.parseInt(args[0]);
			nproc = Integer.parseInt(args[1]);
		}
		if(args.length == 3)
		{
			nbody = Integer.parseInt(args[0]);
			nproc = Integer.parseInt(args[1]);
			emitBodies = true;
		}

		// Create new BH object
		BarnesHut bh = new BarnesHut(nbody, nproc, emitBodies);

		// Initialize the system
		bh.initSystem(nbody);

		// Do the simulation
		bh.doSimulation();
	}

	/**
	 * Initialize the system: Create nbody bodies with random mass and
	 * position.
	 *
	 * @param nbody  Number of bodies in the simulation
	 */
	public void initSystem(int nbody) throws Exception {
		// Accumulated center of mass
		Vector cmr = new Vector();
		// Accumulated velocity
		Vector cmv = new Vector();

		//emit the input.txt which is input to splash code
		/*File f = new File(nbody+"_input.txt");
        FileOutputStream fout = new FileOutputStream(f);
        PrintStream pStr = new PrintStream(new BufferedOutputStream(fout));
        pStr.println(nbody);
        pStr.println(Constants.NDIM);
        pStr.println("0");*/

		// Fill in the tree
		tree.rmin.SETVS(-2.0);
		tree.rsize = -2.0 * -2.0;  // t->rmin.elts[0];
		tree.bodies = new Body<[i]>[nbody]#i;

		// Create an array of empty bodies
		for (int i = 0; i < nbody; ++i) {
			final int j = i;
			tree.bodies[j] = new Body<[j]>();
		}

		// Fill in the bodies, accumulating total mass and velocity.
		// For some reason we are creating 32 distinct groups of
		// bodies, each with its own "seed factor."
		for (int i = 0; i < 32; i++) {
			uniformTestdata(i, cmr, cmv);
		}

		// Normalize coordinates so average pos and vel are 0
		cmr.DIVVS(cmr, (double) nbody);
		cmv.DIVVS(cmv, (double) nbody);
		for (int i = 0; i < tree.bodies.length; ++i) {
			final int j = i;
			Body<[j]> p = tree.bodies[j];
			p.pos.SUBV(p.pos, cmr); 
			p.vel.SUBV(p.vel, cmv);
			p.index = i;
			//            p.cost = 1;
		}

		/*        //emit body masses to input file
        for(int i = 0; i < tree.bodies.length; i++)
        {
            Body p = tree.bodies[i];
            //pStr.println("0.25");
            DecimalFormat form = new DecimalFormat("#.000000");
            pStr.println(form.format(p.mass));
            //pStr.println((float)1.0/(float)(nbody/32.0));
        }
        for(int i = 0; i < tree.bodies.length; i++)
        {
            Body p = tree.bodies[i];
            for(int j = 0; j < Constants.NDIM; j++)
            {
                pStr.print(p.pos.elts[j]);
                pStr.print(" ");
            }
            pStr.println("");
        }
        for(int i = 0; i < tree.bodies.length; i++)
        {
            Body p = tree.bodies[i];
            for(int j = 0; j < Constants.NDIM; j++)
            {
                pStr.print(p.vel.elts[j]);
                pStr.print(" ");
            }
            pStr.println("");
        }
        pStr.close();*/

		//calculate bounding box once instead of expanding it everytime
		tree.setRsize();
	}

	/**
	 * Carry out the simulation
	 */
	public void doSimulation() throws InterruptedException {
		double tnow;
		double tout;
		int i, nsteps;

		/* Go through sequence of iterations */
		tnow = 0.0;
		i = 0;
		nsteps = Constants.NSTEPS;
		assert(Util.chatting("About to perform %d iters from %f to %f by %f\n",
				nsteps,tnow,Constants.tstop,Constants.dtime));

		tree.count = 0;
		long start = System.nanoTime();

		//create threads and barrier
		/*        tree.barrier = new CyclicBarrier(tree.N);
        tree.barMakeTree = new CyclicBarrier(tree.N);
        tree.barComputeGrav = new CyclicBarrier(tree.N);
        tree.barPosUpdate = new CyclicBarrier(tree.N);
        Thread th[] = new Thread[tree.N - 1];
        for(int n = 0; n < (tree.N - 1); n++)
        {
            th[n] = new Thread(new SlaveStart(n+1, tree, tnow));
            th[n].start();
        }
		 */
		i = 0;
		while ((tnow < Constants.tstop + 0.1*Constants.dtime) && (i < Constants.NSTEPS)) {
			tree.stepsystem(0, i); 
			tnow = tnow + Constants.dtime;
			assert(Util.chatting("tnow = %f sp = 0x%x\n", tnow, 0));
			i++;
		}

		/*       for(int n = 0; n < (tree.N - 1); n++)
        {
            th[n].join();
        }
		 */
		long end = System.nanoTime();
		if(!tree.printBodies)
		{
			System.out.println("Overall time taken for force calculation: " + tree.count);
			System.out.print("Overall time taken for entire program: ");
			System.out.println((end-start)/1000000000.0);
		}
	}

	/**
	 * Create uniform test data for a segment of tree.bodies.
	 *
	 * @param nbodyx      Number of bodies to fill in starting at nbodyx *
	 *                    segmentNum
	 * @param segmentNum  The number of this segment
	 * @param cmr         Accumulated center of mass
	 * @param cmv         Accumulated velocity
	 */
	private void uniformTestdata(int segmentNum, Vector cmr, Vector cmv) {
		double rsc, vsc, r, v, x, y;
		Body<*> p;
		int i;
		int seedfactor = segmentNum+1;
		double temp, t1;
		double seed = 123.0 * (double) seedfactor;
		int k;
		double rsq, rsc1;
		double rad;
		double coeff;
		int nbodyx = nbody/32; 
		double rockmass = 1.0 / (nbody/32.0);
		int start = nbodyx * segmentNum;

		rsc = 3 * Constants.PI / 16;	        /* set length scale factor  */
		vsc = Math.sqrt(1.0 / rsc);		/* and recip. speed scale   */

		for (i = 0; i < nbodyx; i++) {	        /* loop over particles      */
			/* fetch body from previously created array */
			p = tree.bodies[start+i]; 
			//p.mass = 1.0 / nbodyx;			/*   set masses equal       */
			p.mass = rockmass;			/*   set masses equal       */
			seed = Util.rand(seed);
			t1 = Util.xrand(0.0, Constants.MFRAC, seed);
			temp = Math.pow(t1,	                        /*   pick r in struct units */
					-2.0/3.0) - 1;
			r = 1 / Math.sqrt(temp);

			coeff = 4.0; /* exp(log(nbodyx/DENSITY)/3.0); */
			for (k=0; k < Constants.NDIM; k++) {
				seed = Util.rand(seed);
				r = Util.xrand(0.0, Constants.MFRAC, seed);
				p.pos.elts[k] = coeff*r;
			}

			cmr.ADDV(cmr, p.pos);		        /*   add to running sum     */
			do {					/*   select from fn g(x)    */
				seed = Util.rand(seed);
				x = Util.xrand(0.0, 1.0, seed);   	/*     for x in range 0:1   */
				seed = Util.rand(seed);
				y = Util.xrand(0.0, 0.1, seed);  	/*     max of g(x) is 0.092 */
			} while (y > x*x * Math.pow(1 - x*x, 3.5));	/*   using von Neumann tech */
			v = Math.sqrt(2.0) * 
			x / Math.pow(1 + r*r, 0.25);	        /*   find v in struct units */

			rad = vsc*v;                                /*   pick scaled velocity   */

			do {					/* pick point in NDIM-space */
				for (k = 0; k < Constants.NDIM; k++) {	/* loop over dimensions   */
					seed = Util.rand(seed);
					p.vel.elts[k] = 
						Util.xrand(-1.0, 1.0, seed);	/* pick from unit cube  */
				}
				rsq = p.vel.DOTVP(p.vel);		/*   compute radius squared */
			} while (rsq > 1.0);                	/* reject if outside sphere */
			rsc1 = rad / Math.sqrt(rsq);		/* compute scaling factor   */
			p.vel.MULVS(p.vel, rsc1);		        /* rescale to radius given  */
			cmv.ADDV(cmv, p.vel);	      	        /*   add to running sum     */
		}
	}


}
