import java.io.Serializable;
public class Payload implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6625037986217386003L;
	private String message;
	private int[][] board;
	private String playerSelect;
	private int specCount;
	private String name;
	private String gameStats;
	private int x, y;
	public void setBoard(int[][] board) {
		this.board = board;
	}
	public int[][] getBoard() {
		return this.board;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getX() {
		return this.x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getY() {
		return this.y;
	}
	public void setMessage(String s) {
		this.message = s;
	}
	public String getMessage() {
		return this.message;
	}
	public void setPlayer(String s) {
		this.playerSelect = s;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String s) {
		this.name = s;
	}
	public String getPlayer() {
		return this.playerSelect;
	}
	public int getSpecCount() {
		return this.specCount;
	}
	public void setSpecCount(int s) {
		this.specCount = s;
	}
	public void setGameStatText(String s) {
		this.gameStats = s;
	}
	public String getGameStats() {
		return this.gameStats;
	}
	private PayloadType payloadType;
	public void setPayloadType(PayloadType pt) {
		this.payloadType = pt;
	}
	public PayloadType getPayloadType() {
		return this.payloadType;
	}
	
	private int number;
	public void setNumber(int n) {
		this.number = n;
	}
	public int getNumber() {
		return this.number;
	}
	@Override
	public String toString() {
		return String.format("Type[%s], Number[%s], Message[%s]",
					getPayloadType().toString(), getNumber(), getMessage());
	}
}