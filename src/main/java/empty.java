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
	  Configuration config = Configuration.fromCmdLineArguments(args);
	  LogManager logger = BasicLogManager.create(config);
	  ShutdownManager shutdown = ShutdownManager.create();
    
	  //System.out.println(NativeLibraries.getNativeLibraryPath());
	  
	  //ADA ada = new ADA(config, logger, shutdown, Solvers.MATHSAT5);
	  ADA ada = new ADA(config, logger, shutdown, Solvers.SMTINTERPOL);
	  ada.readFromFile("examples/train.ada");
	  //ada.readFromFile("examples/example1.ada");
    
	  long start = System.currentTimeMillis();
	  
	  if(ada.is_empty())
		  System.out.println("-----\nEMPTY\n-----");
	  else
		  System.out.println("---------\nNOT EMPTY\n---------");
	  
	  long end = System.currentTimeMillis();
	  System.out.printf("\n# Time Cost (ms): %s", String.valueOf(end - start));
  }
  
}