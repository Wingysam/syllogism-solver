package syllogism.solver;

import java.util.ArrayList;

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
						Fallacy[] fallacies = syllogism.getFallacies();
						String text = syllogismType + ": ";
						if (fallacies.length > 0) {
							Integer i = 0;
							for (Fallacy fallacy: fallacies) {
								if (i > 0) text += ", ";
								text += fallacy;
								i++;
							}
						} else {
							text += "VALID";
						}
						System.out.println(text);
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
			majorPremiseSubject = Term.MIDDLE_TERM;
			majorPremisePredicate = Term.MAJOR_TERM;
		} else {
			majorPremiseSubject = Term.MAJOR_TERM;
			majorPremisePredicate = Term.MIDDLE_TERM;
		}

		Term minorPremiseSubject;
		Term minorPremisePredicate;
		if (form == 1 || form == 2) {
			minorPremiseSubject = Term.MINOR_TERM;
			minorPremisePredicate = Term.MIDDLE_TERM;
		} else {
			minorPremiseSubject = Term.MIDDLE_TERM;
			minorPremisePredicate = Term.MINOR_TERM;
		}

		return new Syllogism(
				statementFactory.Statement(type.charAt(0), majorPremiseSubject, majorPremisePredicate),
				statementFactory.Statement(type.charAt(1), minorPremiseSubject, minorPremisePredicate),
				statementFactory.Statement(type.charAt(2), Term.MINOR_TERM, Term.MAJOR_TERM));
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

enum Term {
	MAJOR_TERM,
	MINOR_TERM,
	MIDDLE_TERM
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
		return getFallacies().length == 0;
	}

	Fallacy[] getFallacies() {
		ArrayList<Fallacy> fallacies = new ArrayList<Fallacy>();

		if (!isMiddleTermDistributedInAtLeastOnePremise()) {
			fallacies.add(Fallacy.FALLACY_OF_THE_UNDISTRIBUTED_MIDDLE);
		}

		if (!ifMajorTermDistributedInConclusionIsItAlsoDistributedInMajorPremise()) {
			fallacies.add(Fallacy.FALLACY_OF_AN_ILLICIT_MAJOR);
		}

		if (!ifMinorTermDistributedInConclusionIsItAlsoDistributedInMinorPremise()) {
			fallacies.add(Fallacy.FALLACY_OF_AN_ILLICIT_MINOR);
		}

		if (areBothPremisesNegative()) {
			fallacies.add(Fallacy.FALLACY_OF_TWO_NEGATIVE_PREMISES);
		}

		if (hasNegativePremiseAndAffirmativeConclusion()) {
			fallacies.add(Fallacy.FALLACY_OF_A_NEGATIVE_PREMISE_AND_AN_AFFIRMATIVE_CONCLUSION);
		}

		if (hasTwoAffirmativePremisesAndNegativeConclusion()) {
			fallacies.add(Fallacy.FALLACY_OF_TWO_AFFIRMATIVE_PREMISES_AND_A_NEGATIVE_CONCLUSION);
		}

		return fallacies.toArray(new Fallacy[0]);
	}

	Boolean isMiddleTermDistributedInAtLeastOnePremise() {
		return majorPremise.isMiddleTermDistributed() || minorPremise.isMiddleTermDistributed();
	}

	Boolean ifMajorTermDistributedInConclusionIsItAlsoDistributedInMajorPremise() {
		return !conclusion.predicateDistributed || majorPremise.isMajorOrMinorTermDistributed();
	}

	Boolean ifMinorTermDistributedInConclusionIsItAlsoDistributedInMinorPremise() {
		return !conclusion.subjectDistributed || minorPremise.isMajorOrMinorTermDistributed();
	}

	Boolean areBothPremisesNegative() {
		return majorPremise.quality == Quality.NEGATIVE && minorPremise.quality == Quality.NEGATIVE;
	}

	Boolean hasNegativePremiseAndAffirmativeConclusion() {
		return (majorPremise.quality == Quality.NEGATIVE || minorPremise.quality == Quality.NEGATIVE)
				&& conclusion.quality == Quality.AFFIRMATIVE;
	}

	Boolean hasTwoAffirmativePremisesAndNegativeConclusion() {
		return majorPremise.quality == Quality.AFFIRMATIVE && minorPremise.quality == Quality.AFFIRMATIVE
				&& conclusion.quality == Quality.NEGATIVE;
	}
}

enum Fallacy {
	FALLACY_OF_THE_UNDISTRIBUTED_MIDDLE,
	FALLACY_OF_AN_ILLICIT_MAJOR,
	FALLACY_OF_AN_ILLICIT_MINOR,
	FALLACY_OF_TWO_NEGATIVE_PREMISES,
	FALLACY_OF_A_NEGATIVE_PREMISE_AND_AN_AFFIRMATIVE_CONCLUSION,
	FALLACY_OF_TWO_AFFIRMATIVE_PREMISES_AND_A_NEGATIVE_CONCLUSION
}

enum Quality {
	AFFIRMATIVE,
	NEGATIVE
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
		if (subject == Term.MIDDLE_TERM) {
			return subjectDistributed;
		} else if (predicate == Term.MIDDLE_TERM) {
			return predicateDistributed;
		} else {
			throw new Error("Statement does not include a middle term");
		}
	}

	Boolean isMajorOrMinorTermDistributed() {
		if (subject == Term.MIDDLE_TERM) {
			return predicateDistributed;
		} else if (predicate == Term.MIDDLE_TERM) {
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
		this.quality = Quality.AFFIRMATIVE;
	}
}

class StatementE extends Statement {
	StatementE(Term subject, Term predicate) {
		super(subject, predicate);
		this.subjectDistributed = true;
		this.predicateDistributed = true;
		this.quality = Quality.NEGATIVE;
	}
}

class StatementI extends Statement {
	StatementI(Term subject, Term predicate) {
		super(subject, predicate);
		this.subjectDistributed = false;
		this.predicateDistributed = false;
		this.quality = Quality.AFFIRMATIVE;
	}
}

class StatementO extends Statement {
	StatementO(Term subject, Term predicate) {
		super(subject, predicate);
		this.subjectDistributed = false;
		this.predicateDistributed = true;
		this.quality = Quality.NEGATIVE;
	}
}