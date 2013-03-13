package intervals;

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

	public void setInterval(Interval interval) {
		this.m_interval = interval;
	}

	@Override
	public String toString() {
		return "(" + m_var + "," + this.m_interval.toString() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_interval == null) ? 0 : m_interval.hashCode());
		result = prime * result + ((m_var == null) ? 0 : m_var.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		VarInterval other = (VarInterval) o;
		return (this.m_var.equals(other.getVar()) && this.m_interval
				.equals(other.getInterval()));
	}

	@Override
	public VarInterval clone() {

		return new VarInterval(new String(m_var), m_interval.clone());
	}
}
