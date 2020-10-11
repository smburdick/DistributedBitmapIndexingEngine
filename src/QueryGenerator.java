import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Objects of this class writes to a file containing a query set.
 * 
 * Assumptions:
 * 1) Point (exact-match) queries select one bin from a random number of attributes.
 * 		an AND/OR operator is used between all bins. 
 * 
 * 2) Range queries select a random number of attributes, and apply an OR operator
 * 		from bin0 to the a random bin for each attribute. An AND/OR is later applied to
 * 		to the resulting bin from each attribute. 
 * 
 * 3) Lines beginning with '#' denote a comment
 * 
 * @author Alexia, David, et al.
 * @version 12/27/2019
 */

public class QueryGenerator extends AbstractWorkloadGenerator {
	public enum Mode {
		POINT_ONLY,	/** only generate point queries */
		RANGE_ONLY,	/** only generate range queries */
		MIXED		/** generate both types of queries */
	}

	/** query types */
	public static final double DEFAULT_AND_LOAD_FACTOR = 0.85;	/** by default, AND queries appear 85% of the time */
	
	/** fields */
	protected long num_queries;		// number of queries to generate in the workload
	protected Zipf zipf_rng_att;	// a random number generator based on Zipf distribution (use for attr)
	protected Zipf zipf_rng_bin;	// a random number generator based on Zipf distribution (use for bin)
	protected Random uniform_rng;	// a random number generator based on uniform distribution
	private long num_pt_queries;	// number of point queries to generate in the workload
	private long num_range_queries;	// number of range queries to generate in the workload
	private long num_attributes;	// number of attributes queried
	private long num_bins;			// number of bins queried
	private long num_and_ops;		// number of ANDs
	private long num_or_ops;		// number of ORs

	/**
	 * Constructs a query generator with the given attributes.
	 * 
	 * @param num_attributes
	 * @param cardinality
	 * @param num_queries
	 * @param skew_att
	 * @param skew_bin
	 */
	public QueryGenerator(int num_attributes, int cardinality, int num_queries, int skew_att, int skew_bin) {
		super(num_attributes, cardinality);
		this.num_queries = num_queries;
		this.zipf_rng_att = new Zipf(num_attributes, skew_att);
		this.zipf_rng_bin = new Zipf(cardinality, skew_bin);
		this.uniform_rng = new Random(System.currentTimeMillis());
		this.num_pt_queries = 0;
		this.num_range_queries = 0;
		this.num_attributes = 0;
		this.num_bins = 0;
		this.num_and_ops = 0;
		this.num_or_ops = 0;
	}


	/**
	 * Writes the queries a file with the given name. Each line contains 
	 * a single point or range query in the following format.
	 * @param file_out 		Name of the output file containing the queries
	 */
	@Override
	public void writeFile(String file_out) {
		this.writeFile(Mode.POINT_ONLY, 0, DEFAULT_AND_LOAD_FACTOR, file_out);
	}

	/**
	 * Writes the queries a file with the given name. Each line contains 
	 * a single point or range query in the following format:
	 * @param query_type		The type of query. Must be one of {POINT_ONLY, RANGE_ONLY, MIXED_MODE}
	 * @param pt_load_factor	Fraction of queries that should be point queries if MIXED_MODE is selected
	 * 								(ignored unless MIXED_MODE is selected)
	 * @param and_load_factor	Fraction of time an AND operator is generated in a query, instead of an OR
	 * @param file_out			Name of the output file containing the queries
	 */
	public void writeFile(Mode query_type, double pt_load_factor, double and_load_factor, String file_out) {
		//initialize point-query load factor
		switch(query_type) {
			case POINT_ONLY:
				pt_load_factor = 1;
				break;
			case RANGE_ONLY:
				pt_load_factor = 0;
				break;
		}

		//build each query
		StringBuilder query_set = new StringBuilder();
		for (int query_id = 0; query_id < this.num_queries; query_id++) {
			String query = (this.uniform_rng.nextDouble() < pt_load_factor) ?
								   this.point(and_load_factor) :
								   this.range(and_load_factor);
			query_set.append(query + "\n");
		}
		
		//open file for writing
		BufferedWriter out;
		try {
			//write queries to file
			out = new BufferedWriter(new FileWriter(file_out));
			out.write(this.metadata());
			out.write(query_set.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Produces a range query.  Format: 
	 * [r,op,att1_bin_start,att1_bin_end]
	 * 	where <op> = & or |
	 * @param and_load_factor	Fraction of time an AND operator is generated in a query, instead of an OR
	 * @return
	 */
	private String range(double and_load_factor) {
		StringBuilder s = new StringBuilder();

		// begin query construction (range query is always an OR)
		s.append("[r,|,");

		// choose number of attributes to involve in the query (use zipf(num_attribute))
		int attr_id = this.zipf_rng_att.next();

		//a range query from [bin_0, ..., bin_high]
		int first_bin = -1;
		int second_bin = -1;
		while (first_bin == second_bin) {
			first_bin = this.rank_bin_map[attr_id][zipf_rng_bin.next()];
			second_bin = this.rank_bin_map[attr_id][zipf_rng_bin.next()];;
		}
		first_bin += (attr_id) * this.cardinality;
		second_bin += (attr_id) * this.cardinality;

		if (first_bin > second_bin) {
			s.append(second_bin + "," + first_bin + "]");
		}
		else {
			s.append(first_bin + "," + second_bin + "]");
		}



		//update stats
		this.num_bins += (second_bin - first_bin + 1);
		this.num_or_ops += (second_bin - first_bin);

		//update stats
		this.num_range_queries++;
		this.num_attributes++;
		return s.toString();
	}


	/**
	 * Produces a point (i.e., exact-match) query.
	 * Format: [p,op,bin1,bin2]
	 *  <op> = & or |
	 * @return
	 */
	private String point(double and_load_factor) {
		StringBuilder s = new StringBuilder();

		// begin query construction
		s.append("[p,");
		if (uniform_rng.nextDouble() < and_load_factor) {
			s.append("&,");
			this.num_and_ops++;
		}
		else {
			s.append("|,");
			this.num_or_ops++;
		}

		// choose number of attributes to involve in the query (use zipf(num_attribute))
		int first_attr = zipf_rng_att.next();
		int second_attr = zipf_rng_att.next();


		// choose a bin from each attribute (use zipf(cardinality) for both)
		int first_bin = this.rank_bin_map[first_attr][zipf_rng_bin.next()];
		first_bin += (first_attr * this.cardinality);

		int second_bin = this.rank_bin_map[second_attr][zipf_rng_bin.next()];
		second_bin += (second_attr * this.cardinality);

		if (first_bin > second_bin) {
			s.append(second_bin + "," + first_bin + "]");
		}
		else {
			s.append(first_bin + "," + second_bin + "]");
		}

		//update stats
		this.num_pt_queries++;
		this.num_attributes += (first_attr == second_attr) ? 1 : 2;
		this.num_bins += 2;
		return s.toString();
	}

	/**
	 * Returns a metadata string containing the stats for this query set
	 * @return
	 */
	private String metadata() {
		StringBuilder s = new StringBuilder();
		s.append("#######################################");
		s.append("\n# Metadata of query set");
		s.append("\n#");
		s.append("\n# Queries");
		s.append("\n# \tTotal: " + this.num_queries);
		s.append("\n# \tPoint: " + this.num_pt_queries + " (" + 100 * ((double) this.num_pt_queries/this.num_queries) +"%)");
		s.append("\n# \tRange: " + this.num_range_queries + " (" + 100 * ((double) this.num_range_queries/this.num_queries) +"%)");
		s.append("\n#");
		s.append("\n# Operators");
		s.append("\n# \tTotal: " + (this.num_and_ops + this.num_or_ops) + " (" + ((double) (this.num_and_ops + this.num_or_ops)/this.num_queries) +" per query)");
		s.append("\n# \tAND: " + this.num_and_ops);
		s.append("\n# \tOR: " + this.num_or_ops);
		s.append("\n#");
		s.append("\n# Attributes");
		s.append("\n# \tTotal queried: " + this.num_attributes);
		s.append("\n#");
		s.append("\n# Bins");
		s.append("\n# \tTotal queried: " + this.num_bins);
		s.append("\n# \tPer query: " + ((double) this.num_bins/this.num_queries));
		s.append("\n#######################################\n");
		return s.toString();
	}
}
