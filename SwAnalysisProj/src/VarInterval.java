public class VarInterval {

	private String		m_var;
	private Interval	m_interval;

	public VarInterval(String var, Interval interval) {
		m_interval = interval;
		m_var = var;
	}

	public Interval getInterval() {
		return m_interval;
	}

	public String getVar() {
		return m_var;
	}

	public void setVar(String m_var) {
		this.m_var = m_var;
	}

	@Override
	public String toString() {
		return "(" + m_var + ", [" + this.m_interval.getLowerBound() + ","
				+ this.m_interval.getUpperBound() + "])";
	}

	@Override
	public boolean equals(Object o) {
		VarInterval other = (VarInterval) o;
		return (this.m_var.equals(other.getVar()));
	}
}
