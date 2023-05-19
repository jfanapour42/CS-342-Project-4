import java.io.Serializable;
import java.util.ArrayList;

public class DataPacket<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = -5366828169862565011L;
	private ArrayList<T> data;
	private int identifier;
	
	public DataPacket(int id) {
		this.data = new ArrayList<>();
		this.identifier = id;
	}
	
	public int getIdentifier() {
		return identifier;
	}
	
	public void addData(T s) {
		data.add(s);
	}
	
	public void addAllData(ArrayList<T> d) {
		for (int i = 0; i < d.size(); i++) {
			data.add(d.get(i));
		}
	}
	
	public void setData(ArrayList<T> d) {
		data = d;
	}
	
	public ArrayList<T> getData() {
		return data;
	}
	
	@Override
	public String toString() {
		String str = String.format("ID: %d -- Data: ", identifier);
		int i = 0;
		while (i < data.size() - 1) {
			str = str + data.get(i) + ", ";
			i++;
		}
		if (i != 0 || data.size() == 1) {
			str = str + data.get(i);
		}
		return str;
	}
}
