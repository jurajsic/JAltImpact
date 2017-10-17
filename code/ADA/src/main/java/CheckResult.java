import org.sosy_lab.java_smt.api.Model;

public class CheckResult {

	Boolean sat;
	Model model;
	
	public CheckResult() {
		sat = false;
	}
	
	public CheckResult(Boolean s, Model m) {
		sat = s;
		model = m;
	}
	
	public String toString() {
		if(!sat)
			return "UNSAT";
		else
			return model.toString();
	}
	
}
