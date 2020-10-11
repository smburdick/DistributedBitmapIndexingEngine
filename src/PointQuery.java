abstract public class PointQuery extends Query {
	protected int colID1;
	protected int colID2;

	/**
	 * Creates a WAH range query
	 * @param operator the operation to perform
	 * @param colID1 ID of column 1
	 * @param colID2 ID of column 2
	 * @pre col1 > -1 && col2 > -1
	 */
	public PointQuery(Operator operator, int colID1, int colID2) {
		super(operator);
		this.colID1 = colID1;
		this.colID2 = colID2;
	}


	@Override
	public String toString() {
		return "[p," + this.operator.symbol + "," + this.colID1 + "," + this.colID2 + "]";
	}

	/**
	 * Takes two compressed vector and performs an AND
	 * operation on them. The results are returned in a bit vector
	 * 
	 * @param vec1 A compressed vector for querying
	 * @param vec2 A compressed vector for querying
	 * @return  the result of vec1 AND vec2
	 * */
	abstract public ActiveBitCollection AndQuery(ActiveBitCollection vec1, ActiveBitCollection vec2);

	/**
	 * Takes two compressed vector and performs an OR
	 * operation on them. The results are returned in a bit vector
	 *
	 * @param vec1 A compressed vector for querying
	 * @param vec2 A compressed vector for querying
	 * @return  the result of vec1 OR vec2
	 **/
	abstract public ActiveBitCollection OrQuery(ActiveBitCollection vec1, ActiveBitCollection vec2);
}
