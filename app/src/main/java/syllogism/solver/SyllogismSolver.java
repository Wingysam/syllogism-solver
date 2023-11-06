package syllogism.solver;

public class SyllogismSolver {
	public static void main(String[] args) {
		char[] moods = { 'A', 'E', 'I', 'O' };
		Integer[] forms = { 1, 2, 3, 4 };
		SyllogismFactory syllogismFactory = new SyllogismFactory();

		for (char majorPremiseMood : moods) {
			for (char minorPremiseMood : moods) {
				for (char conclusionMood : moods) {
					for (Integer form : forms) {
						String syllogismType = "" + majorPremiseMood + minorPremiseMood + conclusionMood + "-" + form;
						Syllogism syllogism = syllogismFactory.Syllogism(syllogismType);
						System.out.println(syllogismType + ": " + syllogism.isValid());
					}
				}
			}
		}
	}
}

class SyllogismFactory {
	Syllogism Syllogism(String type) {
		StatementFactory statementFactory = new StatementFactory();
		Integer form = Character.getNumericValue(type.charAt(4));

		Term majorPremiseSubject;
		Term majorPremisePredicate;
		if (form == 1 || form == 3) {
			majorPremiseSubject = Term.MiddleTerm;
			majorPremisePredicate = Term.MajorTerm;
		} else {
			majorPremiseSubject = Term.MajorTerm;
			majorPremisePredicate = Term.MiddleTerm;
		}

		Term minorPremiseSubject;
		Term minorPremisePredicate;
		if (form == 1 || form == 2) {
			minorPremiseSubject = Term.MinorTerm;
			minorPremisePredicate = Term.MiddleTerm;
		} else {
			minorPremiseSubject = Term.MiddleTerm;
			minorPremisePredicate = Term.MinorTerm;
		}

		return new Syllogism(
				statementFactory.Statement(type.charAt(0), majorPremiseSubject, majorPremisePredicate),
				statementFactory.Statement(type.charAt(1), minorPremiseSubject, minorPremisePredicate),
				statementFactory.Statement(type.charAt(2), Term.MinorTerm, Term.MajorTerm));
	}
}

class StatementFactory {
	Statement Statement(char mood, Term subject, Term predicate) {
		switch (mood) {
			case 'A':
				return new StatementA(subject, predicate);
			case 'E':
				return new StatementE(subject, predicate);
			case 'I':
				return new StatementI(subject, predicate);
			case 'O':
				return new StatementO(subject, predicate);
		}
		throw new Error("Unknown mood " + mood);
	}
}

class Syllogism {
	Statement majorPremise;
	Statement minorPremise;
	Statement conclusion;

	Syllogism(Statement majorPremise, Statement minorPremise, Statement conclusion) {
		this.majorPremise = majorPremise;
		this.minorPremise = minorPremise;
		this.conclusion = conclusion;
	}

	Boolean isValid() {
		if (!isMiddleTermDistributedInAtLeastOnePremise())
			return false;
		if (!ifTermDistributedInConclusionIsItAlsoDistributedInItsPremise())
			return false;
		if (areBothPremisesNegative())
			return false;
		if (hasNegativePremiseAndAffirmativeConclusion())
			return false;
		if (hasTwoAffirmativePremisesAndNegativeConclusion())
			return false;
		return true;
	}

	Boolean isMiddleTermDistributedInAtLeastOnePremise() {
		return majorPremise.isMiddleTermDistributed() || minorPremise.isMiddleTermDistributed();
	}

	Boolean ifTermDistributedInConclusionIsItAlsoDistributedInItsPremise() {
		if (conclusion.subjectDistributed && !minorPremise.isMajorOrMinorTermDistributed()) {
			return false;
		}
		if (conclusion.predicateDistributed && !majorPremise.isMajorOrMinorTermDistributed()) {
			return false;
		}
		return true;
	}

	Boolean areBothPremisesNegative() {
		return majorPremise.quality == Quality.Negative && minorPremise.quality == Quality.Negative;
	}

	Boolean hasNegativePremiseAndAffirmativeConclusion() {
		return (majorPremise.quality == Quality.Negative || minorPremise.quality == Quality.Negative)
				&& conclusion.quality == Quality.Affirmative;
	}

	Boolean hasTwoAffirmativePremisesAndNegativeConclusion() {
		return majorPremise.quality == Quality.Affirmative && minorPremise.quality == Quality.Affirmative
				&& conclusion.quality == Quality.Negative;
	}
}

enum Term {
	MajorTerm,
	MinorTerm,
	MiddleTerm
}

enum Quality {
	Affirmative,
	Negative
}

class Statement {
	Boolean subjectDistributed;
	Boolean predicateDistributed;
	Quality quality;
	Term subject;
	Term predicate;
	Term majorOrMinorTermDistributed;

	Statement(Term subject, Term predicate) {
		this.subject = subject;
		this.predicate = predicate;
	}

	Boolean isMiddleTermDistributed() {
		if (subject == Term.MiddleTerm) {
			return subjectDistributed;
		} else if (predicate == Term.MiddleTerm) {
			return predicateDistributed;
		} else {
			throw new Error("Statement does not include a middle term");
		}
	}

	Boolean isMajorOrMinorTermDistributed() {
		if (subject == Term.MiddleTerm) {
			return predicateDistributed;
		} else if (predicate == Term.MiddleTerm) {
			return subjectDistributed;
		} else {
			throw new Error("Statement has two middle terms");
		}
	}
}

class StatementA extends Statement {
	StatementA(Term subject, Term predicate) {
		super(subject, predicate);
		this.subjectDistributed = true;
		this.predicateDistributed = false;
		this.quality = Quality.Affirmative;
	}
}

class StatementE extends Statement {
	StatementE(Term subject, Term predicate) {
		super(subject, predicate);
		this.subjectDistributed = true;
		this.predicateDistributed = true;
		this.quality = Quality.Negative;
	}
}

class StatementI extends Statement {
	StatementI(Term subject, Term predicate) {
		super(subject, predicate);
		this.subjectDistributed = false;
		this.predicateDistributed = false;
		this.quality = Quality.Affirmative;
	}
}

class StatementO extends Statement {
	StatementO(Term subject, Term predicate) {
		super(subject, predicate);
		this.subjectDistributed = false;
		this.predicateDistributed = true;
		this.quality = Quality.Negative;
	}
}