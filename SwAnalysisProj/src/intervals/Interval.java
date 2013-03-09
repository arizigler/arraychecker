package intervals;
public class Interval {

	private long			m_lowerBound;
	private long			m_upperBound;
	public static long		POSITIVE_INF	= Long.MAX_VALUE;
	public static long		NEGATIVE_INF	= Long.MIN_VALUE;
	public static Interval	EMPTY			= new Interval(0, 0);

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
		long lower = i1.getLowerBound() - i2.getUpperBound();
		long upper = i1.getUpperBound() - i2.getLowerBound();
		return new Interval(lower, upper);
	}

	public static Interval negExpr(Interval i1) {
		long lower = -i1.getUpperBound();
		long upper = -i1.getLowerBound();
		return new Interval(lower, upper);
	}

	public static Interval mul(Interval i1, Interval i2) {
		/*
		 * [a, b] ª [c, d] = [min (a ª c, a ª d, b ª c, b ª d), max (a ª c, a ª
		 * d, b ª c, b ª d)]
		 */
		return new Interval(Math.min(
				Math.min(i1.getLowerBound() * i2.getLowerBound(),
						i1.getLowerBound() * i2.getUpperBound()),
				Math.min(i1.getUpperBound() * i2.getLowerBound(),
						i1.getUpperBound() * i2.getUpperBound())), Math.max(
				Math.max(i1.getLowerBound() * i2.getLowerBound(),
						i1.getLowerBound() * i2.getUpperBound()),
				Math.max(i1.getUpperBound() * i2.getLowerBound(),
						i1.getUpperBound() * i2.getUpperBound())));
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (m_lowerBound ^ (m_lowerBound >>> 32));
		result = prime * result + (int) (m_upperBound ^ (m_upperBound >>> 32));
		return result;
	}

	// @Override
	// public boolean equals(Object obj) {
	// if (this == obj) return true;
	// if (obj == null) return false;
	// if (getClass() != obj.getClass()) return false;
	// Interval other = (Interval) obj;
	// if (m_lowerBound != other.m_lowerBound) return false;
	// if (m_upperBound != other.m_upperBound) return false;
	// return true;
	// }
}
