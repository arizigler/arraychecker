package arraydefs;

import intervals.Interval;

public class ArrayDef {

	private String		m_arrayName;
	private Interval	m_sizeInterval;

	public ArrayDef(String name, Interval interval) {
		super();
		this.m_arrayName = name;
		this.m_sizeInterval = interval;
	}

	public String getName() {
		return m_arrayName;
	}

	public void setName(String name) {
		this.m_arrayName = name;
	}

	public Interval getInterval() {
		return m_sizeInterval;
	}

	public void setInterval(Interval interval) {
		this.m_sizeInterval = interval;
	}

	@Override
	public String toString() {
		return "(" + this.m_arrayName + "," + this.m_sizeInterval.toString()
				+ ")";
	}

	@Override
	public boolean equals(Object obj) {
		ArrayDef other = (ArrayDef) obj;

		return (this.m_arrayName.equals(other.getName()) && this.m_sizeInterval
				.equals(other.getInterval()));
	}

}
