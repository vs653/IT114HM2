import java.util.*;

public class TicTacToeUtility {
	public static boolean isWin(int[][] board, int player) {
		for (int i = 0; i < board.length; i++) {
			int count = 0;
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] == player)
					count++;
			}
			if (count == board.length)
				return true;
			count = 0;
			for (int j = 0; j < board[i].length; j++) {
				if (board[j][i] == player)
					count++;
			}
			if (count == board.length)
				return true;
			count = 0;
		}
		int count = 0;
		for (int i = 0; i < board.length; i++) {
			if (board[i][i] == player)
				count++;
		}
		if (count == board.length)
			return true;
		count = 0;
		for (int i = 0; i < board.length; i++) {
			if (board[board.length - i - 1][i] == player)
				count++;
		}
		if (count == board.length)
			return true;
		return false;
	}
	public static boolean isTie(int[][] board, int player) {
		boolean isFull = true;
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board.length; j++) {
				if(board[i][j] == 0) {
					isFull = false;
				}
			}
		}
		if(isFull && !isWin(board, player)) {
			return true;
		} else {
			return false;
		}
	}
}
