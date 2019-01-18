package comp2402a5;


import java.lang.invoke.ConstantCallSite;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This class implements the cuckoo hash
 *
 * See: Rasmus Pagh, Flemming Friche Rodler, Cuckoo Hashing, Algorithms - ESA 2001,
 * Lecture Notes in Computer Science 2161, Springer 2001, ISBN 3-540-42493-8
 *
 * @param <T>
 */
public class CuckooHashTable<T> extends OpenAddressHashTable<T> {

	/* add any attributes you may need here */

	MultiplicativeHashFunction h1;
	MultiplicativeHashFunction h2;
	ArrayList<Integer> sizeHistory = new ArrayList();
	int[] zz;
	int zIndex = 0;
	int fc = 0;
	int rhc = 0;
	int sac = 0;
	int addC = 0;
	int rc = 0;
	int iters = 0;

	final double epsilon1 = 1 + 1e-32; // 1 + epsilon


	double log(int n){
		double ans = Math.log(n) / Math.log(1 + epsilon1);
		return ((ans <= 0) ? 1 : ans);
	}

	public static void main(String [] args){
		//System.out.println("XXX  = " + (1 << 4));

		int[] zz = new int[]{1445229539, 372911817, 2113534159, 1880685119, 532791015, 1428079773};

		//System.exit(1);

		CuckooHashTable<String> ch = new CuckooHashTable<>(String.class, zz);

/*

		ch.add("eel");
		System.out.println(ch.toString());
		ch.add("dog");
		System.out.println(ch.toString());
		ch.add("cat");
		System.out.println(ch.toString());
		ch.add("cow");
		System.out.println(ch.toString());
		ch.add("elf");
		System.out.println(ch.toString());
		ch.add("elf");
		System.out.println(ch.toString());
		ch.add("elf");
		ch.add("elf");
		ch.add("elf");
		ch.add("elf");
		System.out.println(ch.toString());
*/

		float n = 20;

		/*
		float size = 16;

		for(int i = 0; i < n; i ++){
			if(  ( (i + 1) /size) > 0.5){
				System.out.println("Doubling at i = " + i + " size = " + size);
				size *= 2;
				System.out.println(" Size now = " + size);
				System.out.println("  Inserted Items now = " + i);
			}
		}

		System.out.println("Final size = " + size); // final size ended up being 524,288 for 200k adds (may differ when rehashing is taken into account

		System.exit(1);*/

		for(int i = 0; i < n; i ++){
			try{
				ch.add("elf " + i);
			}catch(StackOverflowError sof){
				System.out.println(Arrays.toString(ch.t));
			}


			//System.out.println("SAC = " + ch.sac);
			//System.out.println("RHC = " + ch.rhc);
			//System.out.println("n = " + ch.n);
			//System.out.println("t.length = " + ch.t.length);
			//System.out.println("Resize Count = " + ch.rc);
			//System.out.println("Size History = " + ch.sizeHistory);
			//System.out.println("Add Count = " + ch.addC);
		}

		//System.out.println("t.length after all adds is " + ch.t.length + " n = " + ch.n);


		System.out.println();
		System.out.println();

		for(int i = 0; i < n; i ++){

			if(ch.find("elf " + i) == null){
				//System.out.println("Couldn't find elf " + i);
				ch.fc++;
			}

		}

		System.out.println("Couldn't Find Count = " + ch.fc);
		System.out.println("Final n = " + ch.n);
		System.out.println("Final t.length = " + ch.t.length);

	}


	/**
	 * Create a new empty hash table
	 * @param clazz is the class of the data to be stored in the hash table
	 * @param zz is an array of integer values to be used for the has functions
	 */
	public CuckooHashTable(Class<T> clazz, int[] zz) {
		super(clazz);

		h1 = new MultiplicativeHashFunction(zz[zIndex], OpenAddressHashTable.w, 4);
		h2 = new MultiplicativeHashFunction(zz[zIndex + 1], OpenAddressHashTable.w, 4);

		//usedZs.add(zz[zIndex]);
		//usedZs.add(zz[zIndex+1]);

		d = 4;
		n = 0;
		f = new Factory<T>(clazz);
		t = f.newArray(1 << d); // d ^ 2
		zIndex += 2;
		this.zz = zz;

	}

	@Override
	protected void resize() {

		rc ++;
		float x = ((float) n + 1) / t.length;

		if (x > 0.5) {
			grow();
			rehash();
			return;
		}

		if((     ((float) n) - 1f) / ((float) t.length) > 1f/8f && d >= 5){
			//System.out.println("Shrink clause in resize!!");
			shrink();
			rehash();
		}

	}

	private void shrink(){

		//T[] temp = f.newArray(t.length / 2);

		T[] tempA = f.newArray( (t.length / 2) );

		if(tempA.length < 16){
			tempA = f.newArray(16);
			d = 4;
		}else{
			this.d --;
		}

		for(int i = 0; i < t.length; i ++){
			if(t[i] != null){
				tempA[i] = t[i];
			}
		}

		t = tempA;
	}

	private void grow(){
		T[] tempA = f.newArray( (t.length * 2) );
		this.d ++;

		//System.out.println("Grew to size " + tempA.length + " from size " + t.length);

		for(int i = 0; i < t.length; i ++){
			if(t[i] != null){
				tempA[i] = t[i];
			}
		}

		t = tempA;
	}

	double maxIters(){
		return 3 * log(n);
	}

	@Override
	public boolean add(T x){
		if(x == null) return false;
		if(find(x) != null) return false;

		float f = (float) (n+1) / (float) t.length;
		if(   f > 0.5  ) grow();

		if(h1.hash(x) == h2.hash(x)) {
			System.out.println("h1.hash(" + x + ") = " + h1.hash(x) + " h2.hash("+x+") = " + h2.hash(x));
			System.out.println("\tt[h1.hash(" + x + ")] = " + t[h1.hash(x)] + " t[h2.hash("+x+")] = " + t[h2.hash(x)]);
			System.out.println("\tn = " + n + " t= " + Arrays.toString(t));
			rehash();
			return add(x);
		}

		T temp = t[h1.hash(x)];
		t[h1.hash(x)] = x;
		int tempPos = h1.hash(x);
		if(temp == null){
			n++;
			return true;
		}

		if(h1.hash(temp) == h2.hash(temp)){
			rehash();
			return add(temp);
		}

		if(t[h1.hash(temp)] != null && t[h2.hash(temp)] != null){
			rehash();
			return add(temp);
		}


		// if it temp came from h2(temp) then check h1 if it came from h1(temp) check h2(temp)

		if(tempPos == h1.hash(temp)){
			if(t[h2.hash(temp)] == null){
				System.out.println("Checking h2");
				t[h2.hash(temp)] = temp;
				n++;
				return true;
			}else{
				T tempFromH2 = t[h2.hash(temp)];
				t[h2.hash(temp)] = temp;
				return add(tempFromH2);
			}
		}else if(tempPos == h2.hash(temp)){
			if(t[h1.hash(temp)] == null){
				System.out.println("Checking h1");
				t[h1.hash(temp)] = temp;
				n++;
				return true;
			}else{
				T tempFromH1 = t[h1.hash(t)];
				t[h1.hash(temp)] = temp;
				return add(tempFromH1);
			}
		}

		rehash();
		return add(temp);
	}

	private void rehash(){
		rhc ++;
		T[] tempA = t;
		clear();

		try{
			h1 = new MultiplicativeHashFunction(zz[zIndex], OpenAddressHashTable.w, d);
			h2 = new MultiplicativeHashFunction(zz[zIndex + 1], OpenAddressHashTable.w, d);
			zIndex += 2;

		}catch(IndexOutOfBoundsException iob){
			System.out.println("IOB");
			zIndex = 0;
			rehash();
		}

		//System.out.println("Entering adds in grow");
		for(T x : tempA){
			if(x != null){

				add(x);

			}
		}
		//System.out.println("After rehash t = " + Arrays.toString(t));

	}
	//<editor-fold>

	@Override
	public void clear() {
		n = 0;
		d = 4;
		t = f.newArray(1 << d);
	}

	@Override
	public int size() {
		return super.size();
	}

	public boolean add2(T x){
		System.out.println("Adding " + x);
		addC ++;

		if(find(x) != null) return false;

		float f = ((float) n + 1) / t.length;

		if (f > 0.5) resize(); // double the size of t and rehash everything!!!

		//<editor-fold desc="Failed Attempt From Journal">
		/*
		double maxLoop = 3 * log(n);

		int count = 0;

		while(count < maxLoop){
			count ++;


			T temp = t[h1.hash(x)];
			t[h1.hash(x)] = x;

			if(temp == null){
				n ++;
				return true;
			}else{
				T temp2 = t[h2.hash(temp)];
				t[h2.hash(temp)] = temp;
				if(temp2 == null){
					return true;
				}
			}


			if(t[h1.hash(x)] == null){
				t[h1.hash(x)] = x;
				n ++;
				return true;
			}

			T temp = t[h1.hash(x)];
			t[h1.hash(x)] = x;

			if(t[h2.hash(temp)] == null){
				t[h2.hash(temp)] = temp;
				return true;
			}

			x = t[h2.hash(temp)];
			t[h2.hash(temp)] = temp;
		}
		//System.out.println("Exited loop. maxLoop = " + maxLoop + " count = " + count);
		rehash();
		return add(x);*/

		//</editor-fold>


		if(t[h1.hash(x)] !=  null){
			//System.out.println("t[h1.hash (" + x + ")] != null");
			T temp = t[h1.hash(x)];


			if(t[h1.hash(temp)] != null && t[h2.hash(temp)] != null){
				rehash();
			}

			if(h1.hash(temp) == h2.hash(temp)){
				//System.out.println("Rehashing");
				//System.out.println("Pre-rehash " + Arrays.toString(t));
				rehash();
				//System.out.println("Post-rehash " + Arrays.toString(t));

				return add(x);
			}else{
				System.out.println(x + " hashes to " + h1.hash(x) + " and " + h2.hash(x));
				System.out.println("These positions are populated by hashes to " + t[h1.hash(x)] + " and " + t[h2.hash(x)]);
				if(t[h2.hash(temp)] != null){
					if(t[h1.hash(temp)] == null){
						t[h1.hash(temp)] = temp;
					}else if(t[h2.hash(temp)] == null){

						t[h2.hash(temp)] = temp;

					}else{
						rehash();
						return add(x);
						//System.out.println("Returning false for x =  " + x);
						//System.out.println(x + " hashes to " + h1.hash(x) + " and " + h2.hash(x));
						//System.out.println("These positions are populated by hashes to " + t[h1.hash(x)] + " and " + t[h2.hash(x)]);
						//fc++;
						//return false;
					}
				}else{
					t[h2.hash(temp)] = temp;
					t[h1.hash(x)] = x;
				}
			}

			// System.out.println("Temp = " + temp);

			//System.out.println("t[h2.hash("+ temp + ")] = " + h2.hash(temp));
			//System.out.println("MOVED " + temp + " from index " + h1.hash(temp) + " to index " + h2.hash(temp));
		}else{
			t[h1.hash(x)] = x;
		}


		n ++;
		return true;

	}

	@Override
	public T remove(T x) {

		if(find(x) == null) return null;

		T temp = null;
		try{
			if(t[h2.hash(x)] != null && t[h2.hash(x)].equals(x)){
				temp = t[h2.hash(x)];
				t[h2.hash(x)] = null;
				n--;
			}else if(t[h1.hash(x)] != null && t[h1.hash(x)].equals(x)){
				temp = t[h1.hash(x)];
				t[h1.hash(x)] = null;
				n --;
			}

		}catch(ArrayIndexOutOfBoundsException iob){
			if(t[h1.hash(x)] != null && t[h1.hash(x)].equals(x)){
				temp = t[h1.hash(x)];
				t[h1.hash(x)] = null;
				n --;
			}
		}

		float f = ((float) n) / (float) t.length;
		//System.out.println("f = " + f);
		//System.out.println("1/8 = " + (1f/8f));

		if(f < (1f/8f) && d >= 5){
			//System.out.println("Shrinks Clause!");
			shrink(); // halve the size of t and rehash everything!!!
		}


		return temp;
	}

	@Override
	public T find(Object x) {
		try {
			if (t[h1.hash(x)] != null && t[h1.hash(x)].equals(x)) {
				return t[h1.hash(x)];
			} else if (t[h2.hash(x)] != null && t[h2.hash(x)].equals(x)) {
				return t[h2.hash(x)];
			} else {
				return null;
			}
		} catch(IndexOutOfBoundsException iob){
			//System.out.println("H1 Params = " + Arrays.toString(h1.getParams()));
			//System.out.println("H2 Params = " + Arrays.toString(h2.getParams()));
			//System.out.println("IOB WHEN FINDING t[h1.hash(" + x + ")] \n\t HASH = " + h1.hash(x) + " LENGTH = " + t.length);
			//System.out.println("H1.d = " + h1.getParams()[2]);
			//System.out.println("H2.d = " + h2.getParams()[2]);
			//System.out.println();
			//System.exit(-1);
			return null;
		}
	}

	@Override
	public Iterator<T> iterator() {
		return super.iterator();
	}

	//</editor-fold>


}
