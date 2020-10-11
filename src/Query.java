
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

abstract public class Query {
	/**
	 * Query Operators
	 * Currently only AND and OR are supported
	 */
	public enum Operator {
		AND("&"), OR("|");

		String symbol;
		Operator(String symb) {
			this.symbol = symb;
		}
	}

	/** static variables */
	public static HashMap<Integer, ActiveBitCollection> columns =
			new HashMap<>();    // bitmaps

	/** instance variables */
	protected CompressedBitmapReader cbr; 	// used to read compressed bitmap files
	protected Operator operator;

	/**
	 * Creates a query of the given operator
	 * @param operator
	 */
	public Query(Operator operator) {
		this.setBitmapReader();
		this.operator = operator;
	}

	/**
	 * Loads a bitmap file into memory
	 * @param colID
	 */
	protected ActiveBitCollection loadFile(int colID) {
		if (Query.columns == null) {
			throw new RuntimeException("This query does not associate with a bitmap.");
		}
		DataInputStream data_in = null;
		try {
			data_in = new DataInputStream(
						new FileInputStream(new File(
								RunConfig.CMP_FULLPATH_TO_COMPRESSED_INDEX +
								RunConfig.CMP_COMPRESSED_INDEX_FILE_PREFIX + colID +
								RunConfig.CMP_COMPRESSED_INDEX_FILE_EXTENSION)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// store it
		ActiveBitCollection ret = this.cbr.readColumn(data_in);
		this.columns.put(colID, ret);
		return ret;
	}

	/**
	 * @return the operator to be performed
	 */
	public Operator getOperator() {
		return this.operator;
	}

	/**
	 * Assigns the proper bitmap reader according to encoding type
	 */
	abstract public void setBitmapReader();

	/**
	 * @return the string representation of this query
	 */
	abstract public String toString();

	/**
	 * Executes the query
	 * @return the result vector
	 */
	abstract public ActiveBitCollection execute();
}

