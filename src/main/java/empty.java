/*
	JAltImpact
    Copyright (C) 2017  Xiao XU & Radu IOSIF

	This file is part of JAltImpact.

    JAltImpact is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
    
    If you have any questions, please contact Xiao XU <xiao.xu.cathiec@gmail.com>.
*/

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
	  int mode = 1;
	  
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
				  if(args[2].equals("-mode1"))
					  mode = 1;
				  else if(args[2].equals("-mode2"))
					  mode = 2;
				  else {
					  System.out.println("# Error : Selected mode is not supported.");
					  return;
				  }
				  if(args.length >= 4) {
					  backStep = Integer.parseInt(args[3].substring(1));
				  }
			  }
		  }
	  }
	  
	  if(inputFile.charAt(inputFile.length() - 2) == 'd') {
		  ADA ada = new ADA(config, logger, shutdown, solver);
		  ada.readFromFile(inputFile);
		  
		  System.out.println("# Input File:\t" + inputFile);
		  if(args.length >= 2)
			  System.out.println("# Solver:\t" + solver);
		  if(args.length >= 3)
			  System.out.println("# Mode:\t" + mode);
		  if(args.length >= 4)
			  System.out.println("# Back Step:\t" + backStep);
		  
		  System.out.println("# Start checking emptiness...\n");
	    
		  long start = System.currentTimeMillis();
		  
		  if(ada.is_empty(backStep, true, mode))
			  System.out.println("-----\nEMPTY\n-----");
		  else
			  System.out.println("---------\nNOT EMPTY\n---------");
		  
		  long end = System.currentTimeMillis();
		  System.out.printf("\n# Time Cost (ms): %s\n", String.valueOf(end - start));
	  }
	  else if(inputFile.charAt(inputFile.length() - 2) == 'p') {
		  PA pa = new PA(config, logger, shutdown, solver);
		  pa.readFromFile(inputFile);
		  
		  System.out.println("# Input File:\t" + inputFile);
		  System.out.println("# Solver:\tMATHSAT5");
		  
		  System.out.println("# Start checking emptiness...\n");
		    
		  long start = System.currentTimeMillis();
		  
		  if(pa.is_empty(backStep, true, mode))
			  System.out.println("-----\nEMPTY\n-----");
		  else
			  System.out.println("---------\nNOT EMPTY\n---------");
		  
		  long end = System.currentTimeMillis();
		  System.out.printf("\n# Time Cost (ms): %s\n", String.valueOf(end - start));
	  }
  }
  
}