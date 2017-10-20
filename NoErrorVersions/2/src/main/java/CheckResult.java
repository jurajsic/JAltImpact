import java.util.ArrayList;

import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Model;

public class CheckResult {

	Boolean value;
	Model model;
	ArrayList<BooleanFormula> interpolants;
	
	public CheckResult() {
		value = null;
		model = null;
		interpolants = new ArrayList<BooleanFormula>();
	}
	
	public String toString() {
		if(!value)
			return "$ Non-Accepting @ Interpolants :\n$ " + interpolants.toString();
		else
			return "$ Accepting @ Model :\n$" + ( model == null ? "[Empty Model]" : model.toString());
	}
	
}
