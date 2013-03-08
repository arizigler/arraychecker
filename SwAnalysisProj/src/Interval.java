public class Interval {

	private long		m_lowerBound;
	private long		m_upperBound;
	public static long	POSITIVE_INF	= Long.MAX_VALUE;
	public static long	NEGATIVE_INF	= Long.MIN_VALUE;
	public static Interval  EMPTY      = new Interval(0, 0);
	
	public Interval(long lower, long upper) {
		m_lowerBound = lower;
		m_upperBound = upper;
	}
	
	public Interval(Interval inter) {
		m_lowerBound = inter.getLowerBound();
		m_upperBound = inter.getUpperBound();
	}

	public static Interval combine(Interval i1, Interval i2) {
		long lower = Math.min(i1.getLowerBound(), i2.getLowerBound());
		long upper = Math.max(i1.getUpperBound(), i2.getUpperBound());
		return new Interval(lower, upper);
	}
	
	public static Interval addExpr(Interval i1, Interval i2) {
		long lower = i1.getLowerBound() + i2.getLowerBound();
		long upper = i1.getUpperBound() + i2.getUpperBound();
		return new Interval(lower, upper);
	}
	
	public static Interval subExpr(Interval i1, Interval i2) {
		long lower = i1.getLowerBound() - i2.getLowerBound();
		long upper = i1.getUpperBound() - i2.getUpperBound();
		return new Interval(lower, upper);
	}
	public static Interval negExpr(Interval i1) {
		long lower = -i1.getUpperBound();
		long upper = -i1.getLowerBound();
		return new Interval(lower, upper);
	}	

	public long getLowerBound() {
		return m_lowerBound;
	}

	public void setLowerBound(long m_lowerBound) {
		this.m_lowerBound = m_lowerBound;
	}

	public long getUpperBound() {
		return m_upperBound;
	}

	public void setUpperBound(long m_upperBound) {
		this.m_upperBound = m_upperBound;
	}

	@Override
	public boolean equals(Object obj) {
		Interval other = (Interval) obj;
		return (this.m_lowerBound == other.getLowerBound() && this.m_upperBound == other
				.getUpperBound());
	}

	@Override
	public String toString() {
		return "[" + m_lowerBound + "," + m_upperBound + "]";
	}
}
