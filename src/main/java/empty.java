import java.io.IOException;

import org.sosy_lab.common.NativeLibraries;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.SolverException;

public class empty {
	
  public static void main(String[] args)
		  throws
		  InvalidConfigurationException,
		  SolverException,
		  InterruptedException,
		  IOException
  {  
	  Configuration config = Configuration.defaultConfiguration();
	  LogManager logger = BasicLogManager.create(config);
	  ShutdownManager shutdown = ShutdownManager.create();
    
	  //System.out.println(NativeLibraries.getNativeLibraryPath());
	  
	  Solvers solver = Solvers.MATHSAT5;
	  String inputFile = null;
	  int backStep = 1000;
	  
	  if(args.length == 0) {
		  System.out.println("# Error : No input file.");
		  return;
	  }
	  else if(args.length >= 1) {
		  inputFile = args[0];
		  if(args.length >= 2) {
			  if(args[1].equals("-MATHSAT5"))
				  solver = Solvers.MATHSAT5;
			  else if(args[1].equals("-PRINCESS"))
				  solver = Solvers.PRINCESS;
			  else if(args[1].equals("-SMTINTERPOL"))
				  solver = Solvers.SMTINTERPOL;
			  else if(args[1].equals("-Z3"))
				  solver = Solvers.Z3;
			  else {
				  System.out.println("# Error : Selected solver is not supported.");
				  return;
			  }
			  if(args.length >= 3) {
				  backStep = Integer.parseInt(args[2].substring(1));
			  }
		  }
	  }
	  
	  ADA ada = new ADA(config, logger, shutdown, solver);
	  ada.readFromFile(inputFile);
	  
	  System.out.println("# Input File:\t" + inputFile);
	  if(args.length >= 2)
		  System.out.println("# Solver:\t" + solver);
	  if(args.length == 3)
		  System.out.println("# Back Step:\t" + backStep);
	  
	  System.out.println("# Start checking temptiness...\n");
    
	  long start = System.currentTimeMillis();
	  
	  if(ada.is_empty(backStep, true))
		  System.out.println("-----\nEMPTY\n-----");
	  else
		  System.out.println("---------\nNOT EMPTY\n---------");
	  
	  long end = System.currentTimeMillis();
	  System.out.printf("\n# Time Cost (ms): %s", String.valueOf(end - start));
  }
  
}