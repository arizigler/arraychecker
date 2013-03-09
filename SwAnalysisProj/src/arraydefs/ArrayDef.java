package arraydefs;

public class ArrayDef {

	private String	m_name;
	private long	m_size;

	public ArrayDef(String m_name, long m_size) {
		super();
		this.m_name = m_name;
		this.m_size = m_size;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String m_name) {
		this.m_name = m_name;
	}

	public long getSize() {
		return m_size;
	}

	public void setSize(long m_size) {
		this.m_size = m_size;
	}

	@Override
	public String toString() {
		return "(" + this.m_name + "," + this.m_size + ")";
	}

	@Override
	public boolean equals(Object obj) {
		ArrayDef other = (ArrayDef) obj;

		return (this.m_name.equals(other.getName()) && this.m_size == other
				.getSize());
	}

}
