package intervals;

public class Interval {

	private long			m_lowerBound;
	private long			m_upperBound;
	private boolean			m_isBottom;

	public static long		POSITIVE_INF	= Long.MAX_VALUE;
	public static long		NEGATIVE_INF	= Long.MIN_VALUE;

	public static Interval	ZERO			= new Interval(0, 0);
	public static Interval	INF				= new Interval(NEGATIVE_INF,
													POSITIVE_INF);
	public static Interval	RINF			= new Interval(0, POSITIVE_INF);
	public static Interval	LINF			= new Interval(NEGATIVE_INF, 0);
	public static Interval	BOTTOM			= new Interval(0, 0, true);

	public Interval(long lower, long upper) {
		m_lowerBound = lower;
		m_upperBound = upper;
		m_isBottom = false;
	}

	public Interval(Interval inter) {
		m_lowerBound = inter.getLowerBound();
		m_upperBound = inter.getUpperBound();
		m_isBottom = inter.m_isBottom;
	}

	public Interval(long lower, long upper, boolean is_bottom) {
		m_lowerBound = lower;
		m_upperBound = upper;
		m_isBottom = is_bottom;
	}

	public void setBottom(boolean b) {
		m_isBottom = b;
	}

	public boolean isBottom() {
		return m_isBottom;
	}

	public static Interval getIntersection(Interval i1, Interval i2) {
		if (i1 == null || i2 == null) return null;

		if (i1.isBottom() || i2.isBottom() || !intersect(i1, i2)) return Interval.BOTTOM;

		return new Interval(Math.max(i1.getLowerBound(), i2.getLowerBound()),
				Math.min(i1.getUpperBound(), i2.getUpperBound()));

	}

	public static boolean intersect(Interval i1, Interval i2) {
		if (i1 == null || i2 == null) return false;
		if (i1.isBottom() || i2.isBottom()) return false;

		if ((i1.getUpperBound() < i2.getLowerBound())
				|| (i2.getUpperBound() < i1.getLowerBound())) return false;

		return true;
	}

	public static Interval toMinArrayIndex(Interval i) {

		if (i == null || i.getLowerBound() < 0) return null;
		if (i.isBottom()) return Interval.BOTTOM;
		if (i.equals(Interval.ZERO)) return Interval.ZERO;

		return new Interval(0, i.getLowerBound() - 1);
	}

	public static Interval toMaxArrayIndex(Interval i) {

		if (i == null || i.getLowerBound() < 0) return null;
		if (i.isBottom()) return Interval.BOTTOM;

		if (i.equals(Interval.ZERO)) return Interval.ZERO;

		return new Interval(0, i.getUpperBound() - 1);
	}

	/**
	 * @return true if i1 is completely before i2 on x axis, false otherwise.
	 */
	public static boolean isBefore(Interval i1, Interval i2) {

		if (i1 == null || i2 == null) return false;
		if (intersect(i1, i2)) return false;

		return (i1.getUpperBound() < i2.getLowerBound());

	}

	public static Interval combine(Interval i1, Interval i2) {
		/* [a,b] U [c,d] = [min(a,c), max(b,d)] */
		if ((i1 != null && i1.isBottom()) && (i2 != null && i2.isBottom())) return Interval.BOTTOM;
		if ((i1 != null && i1.isBottom()) && (i2 != null && !i2.isBottom())) return new Interval(
				i2);
		if ((i1 != null && !i1.isBottom()) && (i2 != null && i2.isBottom())) return new Interval(
				i1);
		if (i1 == null || i2 == null) return new Interval(NEGATIVE_INF,
				POSITIVE_INF);

		long lower = Math.min(i1.getLowerBound(), i2.getLowerBound());
		long upper = Math.max(i1.getUpperBound(), i2.getUpperBound());
		return new Interval(lower, upper);
	}

	public static Interval convergentInterval(Interval oldInter,
			Interval newInter) {
		if (oldInter.isBottom() || newInter.isBottom()) return Interval.BOTTOM;
		if (newInter.getUpperBound() > oldInter.getUpperBound()
				&& !(newInter.getLowerBound() < oldInter.getLowerBound())) { return new Interval(
				oldInter.getLowerBound(), POSITIVE_INF); }
		if (newInter.getLowerBound() < oldInter.getLowerBound()
				&& !(newInter.getUpperBound() > oldInter.getUpperBound())) { return new Interval(
				NEGATIVE_INF, oldInter.getUpperBound()); }
		if (!(newInter.getUpperBound() > oldInter.getUpperBound())
				&& !(newInter.getLowerBound() < oldInter.getLowerBound())) { return new Interval(
				newInter); }

		return new Interval(NEGATIVE_INF, POSITIVE_INF);
	}

	public static Interval addExpr(Interval i1, Interval i2) {
		/* [a,b] + [c,d] = [a + c, b + d] */
		if ((i1 != null && i1.isBottom()) || (i2 != null && i2.isBottom())) return Interval.BOTTOM;
		if (i1 == null || i2 == null) return new Interval(NEGATIVE_INF,
				POSITIVE_INF);

		long lower, upper;
		if (i1.getLowerBound() == NEGATIVE_INF
				|| i2.getLowerBound() == NEGATIVE_INF) lower = NEGATIVE_INF;
		else lower = i1.getLowerBound() + i2.getLowerBound();
		if (i1.getUpperBound() == POSITIVE_INF
				|| i2.getUpperBound() == POSITIVE_INF) upper = POSITIVE_INF;
		else upper = i1.getUpperBound() + i2.getUpperBound();
		return new Interval(lower, upper);
	}

	public static Interval subExpr(Interval i1, Interval i2) {
		/* [a,b] - [c,d] = [a - d, b - c] */
		if ((i1 != null && i1.isBottom()) || (i2 != null && i2.isBottom())) return Interval.BOTTOM;
		if (i1 == null || i2 == null) return new Interval(NEGATIVE_INF,
				POSITIVE_INF);
		long lower, upper;
		if (i1.getLowerBound() == NEGATIVE_INF
				|| i2.getUpperBound() == POSITIVE_INF) lower = NEGATIVE_INF;
		else lower = i1.getLowerBound() - i2.getUpperBound();
		if (i1.getUpperBound() == POSITIVE_INF
				|| i2.getLowerBound() == NEGATIVE_INF) upper = POSITIVE_INF;
		else upper = i1.getUpperBound() - i2.getLowerBound();
		return new Interval(lower, upper);
	}

	public static Interval negExpr(Interval i1) {

		/* -[a,b] = [-b, -a] */
		if (i1 != null && i1.isBottom()) return Interval.BOTTOM;
		if (i1 == null) return new Interval(NEGATIVE_INF, POSITIVE_INF);
		long lower = -i1.getUpperBound();
		long upper = -i1.getLowerBound();
		if (i1.getUpperBound() == POSITIVE_INF) lower = NEGATIVE_INF;
		if (i1.getUpperBound() == NEGATIVE_INF) lower = POSITIVE_INF;
		if (i1.getLowerBound() == POSITIVE_INF) upper = NEGATIVE_INF;
		if (i1.getLowerBound() == NEGATIVE_INF) upper = POSITIVE_INF;

		return new Interval(lower, upper);
	}

	public static Interval mul(Interval i1, Interval i2) {
		/*
		 * [a, b] * [c, d] = [min (a * c, a * d, b * c, b * d), max (a * c, a *
		 * d, b * c, b * d)]
		 */
		if ((i1 != null && i1.isBottom()) || (i2 != null && i2.isBottom())) return Interval.BOTTOM;
		if (i1 == null || i2 == null) return new Interval(NEGATIVE_INF,
				POSITIVE_INF);

		long l1 = i1.getLowerBound(), l2 = i2.getLowerBound();
		long u1 = i1.getUpperBound(), u2 = i2.getUpperBound();

		if (l1 != NEGATIVE_INF && l2 != NEGATIVE_INF && u1 != POSITIVE_INF
				&& u2 != POSITIVE_INF) return new Interval(
				Math.min(Math.min(l1 * l2, l1 * u2), Math.min(u1 * l2, u1 * u2)),
				Math.max(Math.max(l1 * l2, l1 * u2), Math.max(u1 * l2, u1 * u2)));

		long lower = 0, upper = 0;

		/* negative INF */
		if ((l1 == NEGATIVE_INF && (l2 > 0 || u2 > 0))
				|| (u1 == POSITIVE_INF && (l2 < 0 || u2 < 0))
				|| (l2 == NEGATIVE_INF && (l1 > 0 || u1 > 0))
				|| (u2 == POSITIVE_INF && (l1 < 0 || u1 < 0))) lower = NEGATIVE_INF;

		/* positive INF */
		if ((u1 == POSITIVE_INF && (l2 > 0 || u2 > 0))
				|| (l1 == NEGATIVE_INF && (l2 < 0 || u2 < 0))
				|| (u2 == POSITIVE_INF && (l1 > 0 && u1 > 0))
				|| (l2 == NEGATIVE_INF && (l1 < 0 || u1 < 0))) upper = POSITIVE_INF;

		if (lower == NEGATIVE_INF && upper == POSITIVE_INF) return new Interval(
				lower, upper);

		if (lower != NEGATIVE_INF && upper != POSITIVE_INF) {
			/*
			 * upper and lower not infinite and we have at least one val INF
			 * (otherwise we won't get here) then the only option to get here is
			 * if one of the intervals is [0,0]
			 */
			return new Interval(0, 0);
		}

		if (lower != NEGATIVE_INF) {
			/* we have infinite upper */
			lower = POSITIVE_INF;
			if (l1 != NEGATIVE_INF) {
				if (l2 != NEGATIVE_INF) lower = Math.min(lower, l1 * l2);
				if (u2 != POSITIVE_INF) lower = Math.min(lower, l1 * u2);
			}
			if (u1 != POSITIVE_INF) {
				if (l2 != NEGATIVE_INF) lower = Math.min(lower, u1 * l2);
				if (u2 != POSITIVE_INF) lower = Math.min(lower, u1 * u2);
			}
		}

		else if (upper != POSITIVE_INF) {
			/*
			 * we should get here only if we have infinite lower but not
			 * infinite upper
			 */
			upper = NEGATIVE_INF;
			if (l1 != NEGATIVE_INF) {
				if (l2 != NEGATIVE_INF) upper = Math.max(upper, l1 * l2);
				if (u2 != POSITIVE_INF) upper = Math.max(upper, l1 * u2);
			}
			if (u1 != POSITIVE_INF) {
				if (l2 != NEGATIVE_INF) upper = Math.max(upper, u1 * l2);
				if (u2 != POSITIVE_INF) upper = Math.max(upper, u1 * u2);
			}
		}
		return new Interval(lower, upper);
	}

	public static Interval div(Interval i1, Interval i2) {
		if ((i1 != null && i1.isBottom()) || (i2 != null && i2.isBottom())) 
			return Interval.BOTTOM;
		if (i1 == null || i2 == null) 
			return new Interval(NEGATIVE_INF,POSITIVE_INF);
		long l1 = i1.getLowerBound(), l2 = i2.getLowerBound();
		long u1 = i1.getUpperBound(), u2 = i2.getUpperBound();

		if (l2 == 0 && u2 == 0)
		/* dividing by zero --> [-INF,INF] */
			return new Interval(NEGATIVE_INF, POSITIVE_INF);

		if ((l2 <= -1 && u2 >= -1) && (l2 <= 1 && u2 >= 1)) {
			// Division by an interval containing zero is not defined under the basic interval arithmetic
			if (l1 == POSITIVE_INF || l1 == NEGATIVE_INF || u1 == POSITIVE_INF || u1 == NEGATIVE_INF) {
				return new Interval(NEGATIVE_INF,POSITIVE_INF);
			}
			long val = Math.max(Math.abs(l1), Math.abs(u1));
			return new Interval(-val,val);
		}
		
		if (l2 == 0) l2 = 1;
		if (u2 == 0) u2 = -1;

		if (l1 != NEGATIVE_INF && l2 != NEGATIVE_INF && u1 != POSITIVE_INF
				&& u2 != POSITIVE_INF) { return new Interval(
				Math.min(Math.min(l1 / l2, l1 / u2), Math.min(u1 / l2, u1 / u2)),
				Math.max(Math.max(l1 / l2, l1 / u2), Math.max(u1 / l2, u1 / u2))); }

		long lower = 0, upper = 0;

		/* negative INF */
		if ((l1 == NEGATIVE_INF && (l2 > 0 || u2 > 0)) || u1 == POSITIVE_INF
				&& (l2 < 0 || u2 < 0)) lower = NEGATIVE_INF;

		/* positive INF */
		if ((l1 == NEGATIVE_INF && (l2 < 0 || u2 < 0)) || u1 == POSITIVE_INF
				&& (l2 > 0 || u2 > 0)) upper = POSITIVE_INF;

		if (lower == NEGATIVE_INF && upper == POSITIVE_INF) return new Interval(
				lower, upper);

		if (lower != NEGATIVE_INF && upper != POSITIVE_INF) {
			/*
			 * if we got here, the interval of i1 surely not INF/-INF and the
			 * interval of i2 doesn't [0,_] or [_,0]
			 */
			lower = Math.min(Math.min(l1 / l2, l1 / u2),
					Math.min(u1 / l2, u1 / u2));
			upper = Math.max(Math.max(l1 / l2, l1 / u2),
					Math.max(u1 / l2, u1 / u2));
		}

		else if (lower != NEGATIVE_INF) {
			/* we have infinite upper */
			lower = POSITIVE_INF;
			if (l1 != NEGATIVE_INF) {
				lower = Math.min(Math.min(l1 / l2, l1 / u2), lower);
			}
			if (u1 != POSITIVE_INF) {
				lower = Math.min(Math.min(u1 / l2, u1 / u2), lower);
			}
		}

		else if (upper != POSITIVE_INF) {
			/*
			 * we should get here only if we have infinite lower but not
			 * infinite upper
			 */
			upper = NEGATIVE_INF;
			if (l1 != NEGATIVE_INF) {
				upper = Math.max(Math.max(l1 / l2, l1 / u2), upper);
			}
			if (u1 != NEGATIVE_INF) {
				upper = Math.max(Math.max(u1 / l2, u1 / u2), upper);
			}
		}
		return new Interval(lower, upper);
	}

	public static Interval meet(Interval i) {

		if (i.getLowerBound() <= i.getUpperBound()) return i;

		return Interval.BOTTOM;
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
		if (this.m_isBottom && other.isBottom()) { return true; }
		if ((this.m_isBottom && !other.isBottom())
				|| (!this.m_isBottom && other.isBottom())) { return false; }
		return (this.m_lowerBound == other.getLowerBound() && this.m_upperBound == other
				.getUpperBound());
	}

	@Override
	public String toString() {
		String lower = String.valueOf(m_lowerBound);
		String upper = String.valueOf(m_upperBound);

		if (this.isBottom()) return "BOTTOM";

		if (m_lowerBound == NEGATIVE_INF) lower = "-INF";
		if (m_upperBound == POSITIVE_INF) upper = "INF";

		if (m_lowerBound == POSITIVE_INF) {
			lower = "INF";
			upper = "INF";
		}

		if (m_upperBound == NEGATIVE_INF) {
			lower = "-INF";
			upper = "-INF";
		}

		return "[" + lower + "," + upper + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (m_lowerBound ^ (m_lowerBound >>> 32));
		result = prime * result + (int) (m_upperBound ^ (m_upperBound >>> 32));
		return result;
	}

	@Override
	public Interval clone() {
		return new Interval(new Long(m_lowerBound), new Long(m_upperBound),
				new Boolean(m_isBottom));
	}

}
