abstract public class RangeQuery extends Query {
	protected final static int TMP_ID = -1;	// for intermediate storage in bitmap

	protected int startColID;
	protected int endColID;

	/**
	 * Creates a WAH range query
	 * @param operator the operation to perform
	 * @param start ID of start column
	 * @param end   ID of end column
	 * @pre start <= end
	 */
	public RangeQuery(Operator operator, int start, int end) {
		super(operator);
		if (this.startColID > this.endColID) {
			throw new IllegalArgumentException("Invalid range: " + start + " to "+ end);
		}
		this.startColID = start;
		this.endColID = end;
	}

	/**
	 * @return ID of the start column
	 */
	public int getStart() {
		return this.startColID;
	}

	/**
	 * @return ID of the end column
	 */
	public int getEnd() {
		return this.endColID;
	}

	/**
	 * @return number of vectors requested in the range
	 */
	public int getSize() {
		return (this.endColID - this.startColID + 1);
	}

	@Override
	public String toString() {
		return "[r," + this.operator.symbol + "," + this.startColID + "," + this.endColID + "]";
	}
}