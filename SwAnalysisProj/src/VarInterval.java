public class VarInterval {

	private long	m_lowerBound;
	private long	m_upperBound;
	private String	m_var;

	public VarInterval(long lower, long upper, String var) {
		m_lowerBound = lower;
		m_upperBound = upper;
		m_var = var;
	}

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

	public String getVar() {
		return m_var;
	}

	public void setVar(String m_var) {
		this.m_var = m_var;
	}

	public void add(VarInterval other) {
		m_lowerBound += other.getLowerBound();
		m_upperBound += other.getUpperBound();
	}

	@Override
	public String toString() {
		return "(" + m_var + ", [" + m_lowerBound + "," + m_upperBound + "])";
	}

	@Override
	public boolean equals(Object o) {
		VarInterval other = (VarInterval) o;
		return this.m_var.equals(other.getVar());
	}

}
