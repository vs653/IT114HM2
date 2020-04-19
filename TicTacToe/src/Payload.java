import java.io.Serializable;
import javax.swing.*;
public class Payload implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6625037986217386003L;
	private String message;
	private JButton[][] move;
	public void setMessage(String s) {
		this.message = s;
	}
	public String getMessage() {
		return this.message;
	}
	public void setMove(JButton[][] move) {
		this.move = move;
	}
	public JButton[][] getMove() {
		return this.move;
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