import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

class Item {
	String name;
	int amount;
	double price;
	String id;
	
	public Item(String id) {
		this.id = id;
		this.name = "Empty";
	}
	
	public Item(String id, String name, int amount, double price) {
		this.name = name;
		this.amount = amount;
		this.price = price;
		this.id = id;
	}
	
}

public class Vending {
	public static boolean end = false;
	public static int count=1;
	private final static String[] letters = {"A","B","C","D","E","F","G","H","I","J"}; // for vending machine rows, assuming a vending machine won't have more than 10 rows

	public static void main(String[] args) throws IOException { // open log(file) and write "Program start"
		JSONParser parser = new JSONParser(); 
		Item[][] items = null; // items for vending machine
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in); // scanner to read input
		String option;
		File myobj = new File("log.txt");
		myobj.createNewFile();
		FileWriter myWriter = new FileWriter("log.txt");
		log(myWriter,"Program start");
		do {
			do {
				System.out.println("Pick an Option:\n1. Update Vending Machine\n2. Buy an item\n3. View items\n4. End the program");
				option = scanner.nextLine();
				System.out.println();
			} while (!option.equals("1")&&!option.equals("2")&&!option.equals("3")&&!option.equals("4"));
			
			switch(option) {
				case "1":
					boolean correctFile;
					do {
						System.out.print("Enter the name of the JSON file: ");
						String file = scanner.nextLine();
						System.out.println();
						try {
							correctFile = true;
							FileReader reader = new FileReader("./jsonfiles/"+file);
							Object obj = parser.parse(reader);
							JSONObject jsonObj = (JSONObject) obj;
							
							items = populateMachine((JSONObject)jsonObj.get("config"), (JSONArray)jsonObj.get("items")); // log: "updated with (file)"
							log(myWriter,"Update with json file");
						} catch (Exception e) {
							System.out.println("File name does not exist! Make sure the json is inside /jsonfiles");
							//e.printStackTrace();
							correctFile = false;
						}
					} while (!correctFile);
					break;
				case "2":
					if(items == null) {
						System.out.println("Please update the vending machine");
						System.out.println();
						break;
					}
					
					String id;
					do {
						System.out.println("Enter a selection (ex. A0): ");
						id = (scanner.nextLine()).toUpperCase();
						System.out.println();//q
						
						if(!contains(items,id)) System.out.println("Item does not exist");
						
					} while (!contains(items,id));
					
					buyItem(items, id,myWriter); // established that item already exists previously
					
					break;
				case "3": 
					if(items == null) {
						System.out.println("Please update the vending machine");
						System.out.println();
						break;
					}
					print(items); // log "vending maching displayed"
					log(myWriter,"Vending Machine Displayed");
					System.out.println();
					break;
				case "4":
					System.out.println("Program end"); // log "Program end", close the log
					end = true;
					log(myWriter,"Program ended");
					myWriter.close();
					break;
			}
		} while (!option.equals("4"));
	}

	
	private static void buyItem(Item[][] items, String id,FileWriter w) throws IOException {
		int row = 0;
		int col = Integer.valueOf(id.substring(1));
		for(int i = 0; i < letters.length; i++)
			if(letters[i].equals(id.substring(0,1))) row = i;
		
		String name = items[row][col].name;
		int amount = items[row][col].amount;
		double price = items[row][col].price;
		
		double payment = 0;
		String input;
		Scanner scanner = new Scanner(System.in);
		System.out.println(name+" selected, at position ("+id+") costs $"+price+". There are "+amount+" remaining."); // log this
		System.out.println();
		
		do {
			if (amount > 0) {
				System.out.println("1. Enter your payment\n2. Return");
				input = scanner.nextLine();
				System.out.println();
				
			} else return;
			
			switch(input) {
				case "1":
					System.out.print("How much money would you like to input (ex. 5 or 5.00): ");
					input = scanner.nextLine(); // log payment
					payment = Double.valueOf(input);
					System.out.println();
					if(payment >= price) {
						items[row][col].amount--; // subtract out 1 item from machine
						
						if(items[row][col].amount == 0) // if empty
							items[row][col] = new Item(id);//q
						
						double change = Math.round((payment-price)*100.0)/100.0;
						System.out.println("The change will be $"+change+". There are "+(amount-1)+" "+name+" remaining."); // log this
						log(w,name+" was bought with $"+payment+". The change will be $"+change+". There are "+(amount-1)+" "+name+" remaining.");
					}
					else
						System.out.println("The value you have entered is insufficient.");
					System.out.println();
					break;
				case "2":
					return;
			}
		} while (payment < price);
	}

	public static void print(Item[][] items) { //q
		System.out.print("\t");
		for(int i = 0; i < items[0].length; i++) {
			System.out.print(i +"\t"+"\t");
		}
		System.out.println();
		for(int i = 0; i < items.length; i++) {
			for(int k = 0; k < items[i].length; k++) {
				if(k == 0) System.out.print(letters[i] + "\t");
				System.out.print((items[i][k]).name +"\t" );
			}
			System.out.println();
		}
	}
	
	public static Item[][] populateMachine(JSONObject obj, JSONArray array) {
		long numRows = (long) obj.get("rows");
		int numCols = Integer.valueOf((String) obj.get("columns"));
		int row = 0, col = 0, count = 0;
		
		Item[][] items = new Item[(int)numRows][numCols];
		
		for(int i = 0; i < (int)numRows*numCols; i++) { // go through size of the whole machine
			
			String id = letters[row]+col;
			Item item;
			if(count < array.size()) {
				String name = (String) ((JSONObject) array.get(i)).get("name");
				long amount = (long) ((JSONObject) array.get(i)).get("amount");
				String price = (String) ((JSONObject) array.get(i)).get("price");
				item = new Item(id,name, (int)amount, Double.valueOf(price.substring(1))); // set item from json
			} else {
				item = new Item(id); // empty slot for any extra space q
			}
			
			
			items[row][col++] = item;
			count++;
			
			if(col == numCols) {
				row++;
				col = 0;
			}
		}
		
		return items;
	}
	
	public static boolean contains(Item[][] items, String id) {
		for(Item[] row: items) {
			for(Item item: row) {
				if(item.id.equals(id)) return true;
			}
		}
		return false;
	}
	public static void log(FileWriter w,String s) throws IOException {
		
		w.write (String.valueOf(count)+"--"+s+"\n");
		count++;
		
	}
	
}