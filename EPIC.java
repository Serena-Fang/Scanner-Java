import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class EPIC {

	public static void main(String[] args) {
		String url = null;
		String query = null;
		int max = 0;

		File file = new File("Stopwords.txt");
		LinkedList<String> stopwords = new LinkedList<String>();
		try {
			Scanner sc = new Scanner(file);
			
			while (sc.hasNextLine()) {
	            String i = sc.nextLine();
	            stopwords.addLast(i);
	        }
	        sc.close();
		} catch (FileNotFoundException e) {
        	e.printStackTrace();
    	}
		
        

		if (args.length < 3){
			System.out.println("Not Efficient Commandline Argument");
			System.exit(0);
		}

		int number = 0;
		while(number < 3) {
			if (args[number].charAt(1) == 'u') {
				String[] urlarray = args[number].split("u");
				url = urlarray[1];
			} else if (args[number].charAt(1) == 'q') {
				String[] queryarray = args[number].split("q");
				String words = "";
				for (int i = 1; i < queryarray[1].length() - 1; i++) {
					words = "" + queryarray[1].charAt(i);
					
				}
				String[] originalQuery = words.split(" ");
				
				for (int j = 0; j < originalQuery.length; j++) {
					if (!stopwords.exists(originalQuery[j]))
						query = query + " " + originalQuery[j];
				}
				
			} else {
				String[] maxarray = args[number].split("m");
				max = Integer.parseInt(maxarray[1]);
			}
			number++;
		}


		Page root = null;
		try {
			root = new Page(url);
		 
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}

		LinkedList<Page> pagesWithQuery = BFS(root, query);
		Page[] results = new Page[pagesWithQuery.size()];
        int i = 0;
        for (Page r: pagesWithQuery){
            results[i] = r;
            i++;
        }

        QuickSort(results);

        for(int j = 0; j < max; j++) {
        	System.out.println("\033[34;47;1m" + results[j].getTitle() + "\033[0m");
        	System.out.println(results[j].getURL());
        	//System.out.println(snippet(results[j], query));

        }

	}

	public static String snippet(Page page, String query) {
		String[] words = page.getText().split(" ");
		String[] qs = query.split(" ");
		String snippet = "";
		Stemmer s = new Stemmer();
		for(int i = 0; i < words.length; i++) {
			for(int j = 0; j < qs.length; j++) {
				if (s.stem(words[i]).equals(s.stem(qs[j]))) {
					for (int k = i - 10; k < i + 10; k++) {
						snippet = snippet + " " + words[k];
					}
				}
			}
		}
		return snippet;
	}

	public static boolean hasQuery(Page page, String query) {
		String[] words = page.getText().split(" ");
		String[] qs = query.split(" ");
		Stemmer s = new Stemmer();
		for(int i = 0; i < words.length; i++) {
			for(int j = 0; j < qs.length; j++) {
				if (s.stem(words[i]).equals(s.stem(qs[j]))) return true;
			}
		}
		return false; 
	}

	public static LinkedList<Page> BFS(Page source, String query) {
		LinkedList<Page> nextToVisit = new LinkedList<Page>();
		LinkedList<Page> visited = new LinkedList<Page>();
		LinkedList<Page> pages = new LinkedList<Page>();
		nextToVisit.addLast(source);
		while (!nextToVisit.isEmpty()) {
			Page page = nextToVisit.removeFirst();

			if (hasQuery(page, query)) {
				boolean duplicate = false;
				for (Page r: pages) {
					if (r.getText().length() == page.getText().length()) {
						duplicate = true;
						break;
					}
				}
				if (!visited.exists(page)){
					pages.addLast(page);
				}
				
			}

			visited.addLast(page);
			for (Page child: page.adjacentTo()) {
				nextToVisit.addLast(child);
			}
		}
		return pages;
	}

	public static void QuickSort(Page[] a) {
        // might shuffle elements to makes sure T is not sorted                                 
        sort(a, 0, a.length - 1);
    }
 
    // quicksort the subarray from a[lo] to a[hi]                                               
    private static void sort(Page[] a, int lo, int hi) {
        if (hi <= lo) return;
        int j = partition(a, lo, hi);
        sort(a, lo, j-1);
        sort(a, j+1, hi);
    }
 
    // partition the subarray a[lo..hi] so that a[lo..j-1] <= a[j] <= a[j+1..hi]                
    // and return the index j.                                                                  
    private static int partition(Page[] a, int lo, int hi) {
	    int i = lo;
        int j = hi + 1;
        Page v = a[lo]; // use the leftmost element for the pivot
	 while (true) {
            // find item on lo to swap                                                          
            while (a[++i].getRelevance() < v.getRelevance())
            	// search lo to hi 
                if (i == hi) break; 
            // find item on hi to swap    // search hi to lo
            while (v.getRelevance() < a[--j].getRelevance()) 
                if (j == lo) break;      // redundant; a[lo] acts as a sentinel              
            // check if pointers cross                                                          
            if (i >= j) break;
            exch(a, i, j);
        }
        // put partitioning item v at a[j]                                                      
        exch(a, lo, j);
        // now, a[lo .. j-1] <= a[j] <= a[j+1 .. hi]                                            
        return j;
    }
 
   /***************************************************************************                 
    *  Helper functions.                                                                        
    ***************************************************************************/
 
    // exchange a[i] and a[j]                                                                   
    private static void exch(Page[] a, int i, int j) {
        Page swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }


}