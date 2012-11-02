package org.cloudbus.cloudsim.incubator.web.workload.freq;

import java.util.ArrayList;
import java.util.List;

/**
 * An set consisting of smaller finite continous intervals.
 * 
 * @author nikolay.grozev
 * 
 */
public class CompositeValuedSet {

    private List<FiniteValuedInterval> subIntervals;

    /**
     * Constr.
     * 
     * @param subintervals
     *            - the intervals tha make up the set.
     */
    public CompositeValuedSet(List<FiniteValuedInterval> subintervals) {
	this.subIntervals = subintervals;
    }

    /**
     * Returns the value for the x element from its subsets.
     * 
     * @param x
     *            - the value.
     * @return the value for the x element from its subsets.
     */
    public double getValue(double x) {
	for (FiniteValuedInterval i : subIntervals) {
	    if (i.contains(x)) {
		return i.getValue();
	    }
	}
	throw new IllegalArgumentException("X=" + x + " is not contained in " + toString());
    }

    /**
     * Returns if x is contained withing any of the subsets/subintervals.
     * 
     * @param x
     *            - the x to search for.
     * @return if x is contained withing any of the subsets/subintervals.
     */
    public boolean contains(double x) {
	for (FiniteValuedInterval i : subIntervals) {
	    if (i.contains(x)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Creates a new instance based on the specified textual representations.
     * 
     * @param intervals
     *            - the intervals' texts.
     * @return a new instance based on the specified textual representations.
     */
    public static CompositeValuedSet createCompositeValuedSet(String... intervals) {
	return createCompositeValuedSet(null, intervals);
    }

    /**
     * Creates a new instance based on the specified textual representations.
     * 
     * @param seed
     *            - the seed to use.
     * @param intervals
     *            - the intervals' texts.
     * @return a new instance based on the specified textual representations.
     */
    public static CompositeValuedSet createCompositeValuedSet(byte[] seed, String... intervals) {
	List<FiniteValuedInterval> subIntervals = new ArrayList<>();
	for (String i : intervals) {
	    subIntervals.add(FiniteValuedInterval.createInterval(i, seed));
	}
	return new CompositeValuedSet(subIntervals);
    }

    @Override
    public String toString() {
	StringBuffer buff = new StringBuffer();
	for (FiniteValuedInterval interval : subIntervals) {
	    buff.append(interval.toString());
	}
	return buff.toString();
    }
}