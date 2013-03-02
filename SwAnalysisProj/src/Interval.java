public class Interval {

	private long	m_lowerBound;
	private long	m_upperBound;

	public long getLowerBound() {
		return m_lowerBound;
	}

	public void setLowerBound(int m_lowerBound) {
		this.m_lowerBound = m_lowerBound;
	}

	public long getUpperBound() {
		return m_upperBound;
	}

	public void setUpperBound(int m_upperBound) {
		this.m_upperBound = m_upperBound;
	}

	public void add(Interval other) {
		m_lowerBound += other.getLowerBound();
		m_upperBound += other.getUpperBound();
	}
}
