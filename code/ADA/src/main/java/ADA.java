import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;

public class ADA {
	
	// JavaSMT configuration and corresponding context
	// ====================
	SolverContext context;
	FormulaManager fmgr;
	BooleanFormulaManager bmgr;
	IntegerFormulaManager imgr;
	
/**
 * make an integer variable
 * @param v : name of integer variable
 */
	IntegerFormula make_int(String v) {
		return imgr.makeVariable(v);
	}
	
/**
 * make a Boolean variable
 * @param v : name of Boolean variable
 */
	BooleanFormula make_bool(String v) {
		return bmgr.makeVariable(v);
	}

/**
 * make an AND Boolean formula
 * @param bs : (can be several) all the Boolean formulae to be put into AND
 */
	BooleanFormula and(BooleanFormula... bs) {
		return bmgr.and(bs);
	}

/**
* make an OR Boolean formula
* @param bs : (can be several) all the Boolean formulae to be put into OR
*/
	BooleanFormula or(BooleanFormula... bs) {
		return bmgr.or(bs);
	}
	
/**
 * parse a SMT format string to build a Boolean formula
 * @param f : SMT format string to be parsed
 */
	BooleanFormula parse(String f) {
		return fmgr.parse("(assert " + f + ")");
	}

/**
 * check if a Boolean formula is satisfiable
 * @param constraint : Boolean formula to be check
 * @return [.sat : Boolean] whether the formula is satisfiable [.model : Model] an example proving the satisfiability
 */	
	CheckResult checkSAT(BooleanFormula constraint)
			throws
			SolverException,
			InterruptedException
	{
	// check if "constraint" is satisfiable:
	// if so, then return a model satisfying "constraint"
	// else, then return UNSAT
		ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS);
		prover.addConstraint(constraint);
		CheckResult result = new CheckResult();
		result.sat = !prover.isUnsat();
		if(result.sat) {
			result.model = prover.getModel();
		}
		return result;
	}
	
	// ====================

/**
 * constructor (empty ADA)
 * @param config : configuration
 * @param logger : log manager
 * @param shutdown : shutdown manager
 * @param solver : selected solver
 */
	public ADA(Configuration config, LogManager logger, ShutdownManager shutdown, Solvers solver)
			throws
			InvalidConfigurationException
	{
		context = SolverContextFactory.createSolverContext(config, logger, shutdown.getNotifier(), solver);
		fmgr = context.getFormulaManager();
		bmgr = fmgr.getBooleanFormulaManager();
		imgr = fmgr.getIntegerFormulaManager();
	}

	// set of states
	ArrayList<BooleanFormula> Q;

	// initial state
	BooleanFormula i;

	// set of final states
	ArrayList<BooleanFormula> F;

	// set of symbols
	ArrayList<String> SIGMA;

	// set of variables
	ArrayList<IntegerFormula> X;
	
	// transitions
	Map<String, BooleanFormula> DELTA;

/**
 * parse a file to assign the ADA
 * @param filename : name of source file
 */
	void readFromFile(String filename)
			throws
			IOException
	{
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String temp = null;
		// read states
		Q = new ArrayList<BooleanFormula>();
		reader.readLine();
		temp = reader.readLine();
		String [] temp2 = temp.split(" ");
		for(int i = 0; i < temp2.length; i++)
			Q.add(make_bool(temp2[i]));
		// read initial state
		reader.readLine();
		reader.readLine();
		temp = reader.readLine();
		i = make_bool(temp);
		// read final state
		F = new ArrayList<BooleanFormula>();
		reader.readLine();
		reader.readLine();
		temp = reader.readLine();
		temp2 = temp.split(" ");
		for(int i = 0; i < temp2.length; i++)
			F.add(make_bool(temp2[i]));
		// read symbols
		SIGMA = new ArrayList<String>();
		reader.readLine();
		reader.readLine();
		temp = reader.readLine();
		temp2 = temp.split(" ");
		for(int i = 0; i < temp2.length; i++)
			SIGMA.add(temp2[i]);
		// read variables
		X = new ArrayList<IntegerFormula>();
		reader.readLine();
		reader.readLine();
		temp = reader.readLine();
		temp2 = temp.split(" ");
		for(int i = 0; i < temp2.length; i++) {
			X.add(make_int(temp2[i]));
			make_int(temp2[i] + '0');
			make_int(temp2[i] + '1');
		}
		// read transitions
		DELTA = new HashMap<String, BooleanFormula>();
		reader.readLine();
		reader.readLine();
		String temp3 = null, temp4 = null;
		while((temp = reader.readLine()) != null && (!temp.equals(""))) {
			temp4 = "";
			while(!(temp3 = reader.readLine()).equals("#")) {
				temp4 = temp4 + temp3.trim() + ' ';
			}
			DELTA.put(temp, parse(temp4));
		}
		// close file
		reader.close();
	}

/**
 * save the ADA into a file 
 * @param filename : name of target file
 * */
	void saveIntoFile(String filename)
			throws
			IOException
	{
		FileWriter writer = new FileWriter(filename);
		// write states
		writer.write("STATES\n");
		for(int i = 0; i < Q.size(); i++) {
			writer.write(Q.get(i).toString());
			if(i != Q.size() - 1)
				writer.write(' ');
		}
		// write initial state
		writer.write('\n');
		writer.write('\n');
		writer.write("INITIAL\n");
		writer.write(i.toString());
		// write final state
		writer.write('\n');
		writer.write('\n');
		writer.write("FINAL\n");
		for(int i = 0; i < F.size(); i++) {
			writer.write(F.get(i).toString());
			if(i != F.size() - 1)
				writer.write(' ');
		}
		// write symbols
		writer.write('\n');
		writer.write('\n');
		writer.write("SYMBOLS\n");
		for(int i = 0; i < SIGMA.size(); i++) {
			writer.write(SIGMA.get(i));
			if(i != SIGMA.size() - 1)
				writer.write(' ');
		}
		// write variables
		writer.write('\n');
		writer.write('\n');
		writer.write("VARIABLES\n");
		for(int i = 0; i < X.size(); i++) {
			writer.write(X.get(i).toString());
			if(i != X.size() - 1)
				writer.write(' ');
		}
		// write transitions
		writer.write('\n');
		writer.write('\n');
		writer.write("TRANSITIONS\n");
		Iterator<Entry<String, BooleanFormula>> iter = DELTA.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, BooleanFormula> entry = (Entry<String, BooleanFormula>) iter.next();
			writer.write(entry.getKey());
			writer.write('\n');
			writer.write(entry.getValue().toString());
			writer.write('\n');
			writer.write('#');
			writer.write('\n');
		}
		// close file
		writer.close();
	}

/** check if the ADA is empty */
	public boolean is_empty()
			throws
			IOException
	{
		saveIntoFile("examples/temp.ada");
	    return true;
	}
}