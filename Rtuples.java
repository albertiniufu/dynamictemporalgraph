
import java.io.*;
import java.util.*;

public class Rtuples {

	public static void main(String args[]) throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		int V = new Integer(br.readLine()); // number of vertices
		int delta = 1; // duration of contact
		TemporalGraph G = new TemporalGraph(V, delta);

		String completeline;
		while ((completeline = br.readLine()) != null) {
			String[] line = completeline.split(" ");
			if (line[0].equals("can_reach")) {
				int u = G.getNodeId(line[1]);
				int v = G.getNodeId(line[2]);
				int t1 = new Integer(line[3]);
				int t2 = new Integer(line[4]);
				System.out.println(G.can_reach(u,v,t1,t2));
			} else if (line[0].equals("print_rtuples")) {
				System.out.print(G);
			} else if (line[0].equals("is_connected")) {
				int t1 = new Integer(line[1]);
				int t2 = new Integer(line[2]);
				System.out.println(G.is_connected(t1,t2));
			} else if (line[0].equals("test_journey")) {
				int u = G.getNodeId(line[1]);
				int v = G.getNodeId(line[2]);
				int t1 = new Integer(line[3]);
				int t2 = new Integer(line[4]);

				ArrayList<Contact> journey = G.reconstruct_journey(u,v,t1,t2);
				System.out.println(journey!=null);
			} else if (line[0].equals("reconstruct_journey")) {
				int u = G.getNodeId(line[1]);
				int v = G.getNodeId(line[2]);
				int t1 = new Integer(line[3]);
				int t2 = new Integer(line[4]);

				ArrayList<Contact> journey = G.reconstruct_journey(u,v,t1,t2);
				if (journey == null) 
					System.out.println("null");
				else { 
					Contact ctt =null;

					for (int i = 0; i < journey.size(); i++) {
						ctt = journey.get(i);
						System.out.print(ctt.u+" ("+ctt.time+")-> ");
					}
					if (ctt != null)
						System.out.println(ctt.v);
				}
			} else if(line[0].equals("multiple_add_contact")) {
				int u = G.getNodeId(line[1]);
				int v = G.getNodeId(line[2]);
				if (line[1].equals("12") ) {
				   System.out.println("12 == "+u);
				}
				if (line[1].equals("0") ) {
					   System.out.println("0 == "+u);
					}
				int t1 = new Integer(line[3]);
				int t2 = new Integer(line[4]);
				for (int t = t1; t < t2; t++) {
					G.add_contact(u, v, t);
				}
				
				
			} else {
				//System.out.println("Adding: (u,v,time)==(" + u + "," + v + "," + time + ")");
				int u = G.getNodeId(line[0]);
				int v = G.getNodeId(line[1]);
				int time = new Integer(line[2]);
				
				G.add_contact(u, v, time);
			}
		}
		//System.out.println(G);

		/*
		for (int i = 0; i < V; i++)
			for (int j = 0; j <V ; j++)
				if (i == j) continue;
				else {
					System.out.println("Printing journey between "+i+" and "+j+" in time interval ["+0+","+10+"]");
					ArrayList<Contact> journey = G.reconstruct_journey(i, j, 0, 10);
					if (journey != null) {
						for (Contact c: journey) {
							System.out.println(c);
						}
					}
				}
		 */

	}
}

class TemporalGraph {

	TreeSet<Rtuple> m[][];
	int V;

	HashMap<String, Integer> names;
	String[] namesInv;
	int delta;

	public TemporalGraph(int V, int delta) {
		this.V = V;
		this.delta = delta;
		m = new TreeSet[V][V];
		for (int i = 0; i < V; i++)
			for (int j = 0; j < V; j++)
				m[i][j] = new TreeSet<>();
		names = new HashMap<>();
		namesInv = new String[V];
	}

	int getNodeId(String name) {
		Integer id = names.get(name);
		if (id == null) {
			id = names.size();
			names.put(name, id);
			namesInv[id] = name;
		}
		return id;
	}

	String getNodeName(int id) {
		return namesInv[id];
	}

	private Iterable<Integer> neighborsOut(int u) {
		TreeSet<Integer> nOut = new TreeSet<>();

		for (int j = 0; j < V; j++) {
			for (Rtuple r : m[u][j]) {
				if (r.u == u)
					nOut.add(r.v);
				else
					nOut.add(r.u);
			}
		}

		return nOut;
	}

	private Iterable<Integer> neighborsIn(int u) {
		TreeSet<Integer> nIn = new TreeSet<>();

		for (int i = 0; i < V; i++) {
			for (Rtuple r : m[i][u]) {
				if (r.u == u)
					nIn.add(r.v);
				else
					nIn.add(r.u);
			}
		}

		return nIn;
	}

	private Rtuple FIND_PREVIOUS(int u, int v, int t) {
		TreeSet<Rtuple> Tuv = m[u][v];
		return Tuv.floor(new Rtuple(u, v, -1, t));
	}

	private Rtuple FIND_NEXT(int u, int v, int t) {
		TreeSet<Rtuple> Tuv = m[u][v];
		return Tuv.ceiling(new Rtuple(u, v, t, -1));
	}

	private void INSERT(int u, int v, int tMinus, int tPlus, int w) {
		TreeSet<Rtuple> Tuv = m[u][v];
		Rtuple newtuple = new Rtuple(u, v, tMinus, tPlus, w);
		if (Tuv.size() == 0) {
			Tuv.add(newtuple);
			return;
		}

		// FIXME: the following code is inefficient... it's just made to work
		LinkedList<Rtuple> toBeRemoved = new LinkedList<>();

		Rtuple inf = FIND_PREVIOUS(u, v, tPlus);
		if (inf == null) {
			inf = Tuv.first();
		}

		for (Rtuple r : Tuv.tailSet(inf, true)) {
			if (r.tMinus <= tPlus) {
				if (r.includes(newtuple))
					toBeRemoved.add(r);
			} else
				break;
		}

		for (Rtuple r : toBeRemoved) {
			Tuv.remove(r);
		}

		if (u == 7 && v ==  2) { 		  
	             if (tMinus == 13 && tPlus == 15)
				  System.err.println(Tuv);

		}
		Tuv.add(newtuple);
	}



	// Algorithm 1
	// add contact(u, v, t): Update information based on a contact from u to v at
	// time t
	public void add_contact(int u, int v, int time) {
		INSERT(u, v, time, time + delta, v);

		TreeSet<NodeTime> D = new TreeSet<>();

		for (int wMinus : neighborsIn(u)) {
			Rtuple prevRtuple = FIND_PREVIOUS(wMinus, u, time);
			if (prevRtuple != null) {
				INSERT(wMinus, v, prevRtuple.tMinus, time + delta, prevRtuple.w);
				D.add(new NodeTime(wMinus, prevRtuple.tMinus, prevRtuple.w));
			}
		}

		for (int wPlus : neighborsOut(v)) {
			Rtuple next = FIND_NEXT(v, wPlus, time + delta);

			if (next != null) {
				INSERT(u, wPlus, time, next.tPlus, v);

				for (NodeTime p : D) {
					int wMinus = p.node;
					int tMinus = p.time;
					int successor = p.successor;
					if (wMinus != wPlus) {
						INSERT(wMinus, wPlus, tMinus, next.tPlus, successor);
					}
				}
			}
		}
	}

	// can reach(u, v, t1 , t2 ): Return true if u can reach v within the
	// subinterval [t1 , t2 ]
	boolean can_reach(int u, int v, int t1, int t2) {
		Rtuple prev = FIND_NEXT(u, v, t1);

		if (prev != null && prev.tPlus <= t2)
			return true;
		else
			return false;
	}

	// is connected(t 1 , t 2 ): Return true if G restricted to the subinterval [t 1
	// , t 2 ] is temporally
	// connected, i.e. all vertices can reach each other within the subinterval [t 1
	// , t 2 ]
	boolean is_connected(int t1, int t2) {

		for (int i = 0; i < V; i++) {
			for (int j = 0; j < V; j++) {
				if (i == j)
					continue;
				if (!can_reach(i, j, t1, t2))
					return false;
			}
		}

		return true;
	}

	// reconstruct journey(u, v, t 1 , t 2 ): Return a journey (if one exists) from
	// u to v occurring
	// within the subinterval [t 1 , t 2 ]
	ArrayList<Contact> reconstruct_journey(int u, int v, int t1, int t2) {
		Rtuple prev = FIND_NEXT(u, v, t1);

		if (prev == null)
			return null;
		else if (prev.tPlus > t2)
			return null;
		else {
			ArrayList<Contact> ret = new ArrayList<>();

			ret.add(new Contact(u, prev.w, prev.tMinus));
			while (prev.w != v) {
				Rtuple next = FIND_NEXT(prev.w, v, prev.tMinus + delta);

				Contact c = new Contact(prev.w, next.w, next.tMinus);
				ret.add(c);
				prev = next;
			}
			return ret;			
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		int C = 0;

		for (int i = 0; i < V; i++)
			for (int j = 0; j < V; j++) {
				C += m[i][j].size();

				for (Rtuple r : m[i][j])
					sb.append(getNodeName(r.u) + " " + getNodeName(r.v) + " " + r.tMinus + " " + r.tPlus + "\n");
			}
		return C + "\n" + V + "\n" + sb.toString();
	}
}

class Contact {
	int u, v, time;
	public Contact(int u, int v, int time) {
		this.u =u; this.v =v ; this.time = time;
	}
	@Override
	public String toString() {
		return "("+u+","+v +","+time+")";
	}
}

class Rtuple implements Comparable<Rtuple> {

	int tMinus, tPlus, u, v;
	int w = -1; // Constructive R-tuple
	
	public Rtuple(int u, int v, int tM, int tP) {
		this.u = u;
		this.v = v;
		tMinus = tM;
		tPlus = tP;
	}

	public Rtuple(int u, int v, int tM, int tP, int w) {
		this.u = u;
		this.v = v;
		tMinus = tM;
		tPlus = tP;
		this.w = w;
	}

	void setW(int w) {
		this.w = w;
	}

	boolean precedes(Rtuple r2) {
		if (tPlus <= r2.tPlus && r2.u == this.v)
			return true;
		else
			return false;
	}

	Rtuple concatenate(Rtuple r2) {
		if (this.precedes(r2)) {
			return new Rtuple(u, r2.v, this.tMinus, r2.tPlus, this.w);
		} else
			return null;
	}

	boolean is_included_in(Rtuple r2) {
		if (u == r2.u && v == r2.v) {
			if (r2.tMinus <= tMinus && tPlus <= r2.tPlus) {
				return true;
			} else
				return false;
		} else
			return false;
	}

	boolean includes(Rtuple r1) {
		return r1.is_included_in(this);
	}

	boolean is_redundant(Set<Rtuple> s) {
		for (Rtuple rline : s)
			if (rline.includes(this))
				return true;

		return false;
	}

	boolean is_minimal(Set<Rtuple> s) {
		return !is_redundant(s);
	}

	@Override
	public int compareTo(Rtuple r) {
		if (r.tMinus < 0 || this.tMinus < 0) {
			return tPlus - r.tPlus;
		} else {
			return tMinus - r.tMinus;
		}
	}

	@Override
	public String toString() {
		return "<" + u + "," + v + "," + tMinus + "," + tPlus + ">";
	}

}

class NodeTime implements Comparable<NodeTime> {
	Integer node;
	Integer time;
	Integer successor;

	public NodeTime(Integer node, Integer time) {
		this.node = node;
		this.time = time;
	}

	public NodeTime(Integer node, Integer time, Integer successor) {
		this.node = node;
		this.time = time;
		this.successor = successor; 
	}

	@Override
	public int compareTo(NodeTime nt) {
		if (node != nt.node)
			return node - nt.node;
		else {
			return time - nt.time;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;

		if (!(o instanceof NodeTime)) {
			return false;
		}
		NodeTime nt = (NodeTime) o;

		if (node == nt.node && time == nt.time)
			return true;
		else
			return false;
	}
}
