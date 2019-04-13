package lexicalAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;


public class LexicalAnalysis {
	private static ArrayList<String> code = new ArrayList<String>();//所有的代码，作为ArrayList存起来
	private static char currentChar;//当前的字符
	private static String currentWord = "";//当前的词组
	private static int totalRow;//总共的行数
	private static int totalColumnInCurrentRow;//当前行的总列数
	private static int currentRow = 0;//当前所在行号
	private static int currentColumn = 0;//当前所在列
	//关键字集合
	@SuppressWarnings("serial")
	private static final HashMap<String, String> KeyWords = new HashMap<String, String>() {{
		put("int", "INT");
		put("float", "FLOAT");
		put("boolean", "BOOLEAN");
		put("struct", "STRUCT");
		put("if", "IF");
		put("else", "ELSE");
		put("do", "DO");
		put("while", "WHILE");
	}};
	//符号表
	private static final ArrayList<String> symbolTable = new ArrayList<String>();
	//token序列
	private static final IdentityHashMap<String, String> tokenSeq = new IdentityHashMap<String, String>();
	
	/**
	 * 在指定的文件路径读取代码文件的内容
	 * @param filepath 指定的文件路径，为绝对路径
	 * @return ArrayList存储的代码内容
	 */
	private static void readTestFileToArrList(String filepath) {
		try {
			File file = new File(filepath);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String linetxt = null;
				while ((linetxt = bufferedReader.readLine()) != null) {
					code.add(linetxt);
				}
				bufferedReader.close();
				read.close();
			}
			else {
				System.out.println("Cannot find specified file.");
			}
		} catch (Exception e) {
			System.out.println("Read File ERROR!");
			e.printStackTrace();
		}
		totalRow = code.size() - 1;
		totalColumnInCurrentRow = code.get(currentRow).length() - 1;
	}
	
	/**
	 * 读取下一个字符，如果当前读头在代码段中，那么读取下一个字符，并把读头后移一位，否贼返回'\0'
	 * @return 代码段的下一个字符，如果读完了，则返回'\0'
	 */
	private static char getNextChar() {
		if ((currentRow > totalRow) || (currentColumn > totalColumnInCurrentRow)) {
			return '\0';
		}
		else if ((currentRow <= totalRow) && (currentColumn < totalColumnInCurrentRow)) {
			currentColumn ++;
			return code.get(currentRow).charAt(currentColumn);
		}
		else if ((currentRow < totalRow) && (currentColumn == totalColumnInCurrentRow)) {
			currentRow ++;
			currentColumn = 0;
			totalColumnInCurrentRow = code.get(currentRow).length() - 1;
			return code.get(currentRow).charAt(currentColumn);
		}
		else {
			return '\0';
		}
	}
	
	/**
	 * 判断一个字符串是不是关键字
	 * @param word给定的字符串
	 * @return true如果是关键字，false如果不是关键字
	 */
	private static boolean isKeyWord(String word) {
		for (String value : KeyWords.keySet()) {
			if (value.equals(word)) {
				return true;
			}
			else {
				continue;
			}
		}
		return false;
	}
	
	/**
	 * 当读到一个字母时的处理程序，判断是否是关键字
	 */
	private static void identifierHandler() {
		currentWord = "";
		currentWord = currentWord + String.valueOf(currentChar);
		currentChar = getNextChar();
		while ((String.valueOf(currentChar)).matches("^[0-9a-zA-Z_]{1,}$")) {
			currentWord = currentWord + String.valueOf(currentChar);
			currentChar = getNextChar();
		}
		if (isKeyWord(currentWord)) {
			tokenSeq.put(new String(KeyWords.get(currentWord)), null);
		}
		else {
			tokenSeq.put("ID", currentWord);
			if (symbolTable.contains(currentWord)) {
				currentWord = "";
				return;
			}
			else {
				symbolTable.add(currentWord);
				currentWord = "";
				return;
			}
		}
	}
	
	/**
	 * 处理无符号数的DFA
	 * 全是if else，写的贼丑
	 */
	private static void numberHandler() {
		currentWord = "";
		currentWord = currentWord + String.valueOf(currentChar);
		currentChar = getNextChar();
		while ((String.valueOf(currentChar)).matches("[0-9]")) {
			currentWord = currentWord + String.valueOf(currentChar);
			currentChar = getNextChar();
		}
		if ((String.valueOf(currentChar)).matches("E")) {
			currentWord = currentWord + String.valueOf(currentChar);
			currentChar = getNextChar();
			if ((String.valueOf(currentChar)).matches("+|-")) {
				currentWord = currentWord + String.valueOf(currentChar);
				currentChar = getNextChar();
				if ((String.valueOf(currentChar)).matches("[0-9]")) {
					currentWord = currentWord + String.valueOf(currentChar);
					currentChar = getNextChar();
					while ((String.valueOf(currentChar)).matches("[0-9]")) {
						currentWord = currentWord + String.valueOf(currentChar);
						currentChar = getNextChar();
					}
					tokenSeq.put(new String("CONST"), currentWord);
					currentWord = "";
					return;
				}
				else {
					Error(currentRow, "floating-point number error");
					currentWord = "";
					return;
				}
			}
			else if ((String.valueOf(currentChar)).matches("[0-9]")) {
				currentWord = currentWord + String.valueOf(currentChar);
				currentChar = getNextChar();
				while ((String.valueOf(currentChar)).matches("[0-9]")) {
					currentWord = currentWord + String.valueOf(currentChar);
					currentChar = getNextChar();
				}
				tokenSeq.put(new String("CONST"), currentWord);
				currentWord = "";
				return;
			}
			else {
				Error(currentRow, "floating-point number error");
				currentWord = "";
				return;
			}
		}
		else if ((String.valueOf(currentChar)).matches("\\.")) {
			currentWord = currentWord + String.valueOf(currentChar);
			currentChar = getNextChar();
			if ((String.valueOf(currentChar)).matches("[0-9]")) {
				currentWord = currentWord + String.valueOf(currentChar);
				currentChar = getNextChar();
				while ((String.valueOf(currentChar)).matches("[0-9]")) {
					currentWord = currentWord + String.valueOf(currentChar);
					currentChar = getNextChar();
				}
				if ((String.valueOf(currentChar)).matches("E")) {
					currentWord = currentWord + String.valueOf(currentChar);
					currentChar = getNextChar();
					if ((String.valueOf(currentChar)).matches("+|-")) {
						currentWord = currentWord + String.valueOf(currentChar);
						currentChar = getNextChar();
						if ((String.valueOf(currentChar)).matches("[0-9]")) {
							currentWord = currentWord + String.valueOf(currentChar);
							currentChar = getNextChar();
							while ((String.valueOf(currentChar)).matches("[0-9]")) {
								currentWord = currentWord + String.valueOf(currentChar);
								currentChar = getNextChar();
							}
							tokenSeq.put(new String("CONST"), currentWord);
							currentWord = "";
							return;
						}
						else {
							Error(currentRow, "floating-point number error");
							currentWord = "";
							return;
						}
					}
					else if ((String.valueOf(currentChar)).matches("[0-9]")) {
						currentWord = currentWord + String.valueOf(currentChar);
						currentChar = getNextChar();
						while ((String.valueOf(currentChar)).matches("[0-9]")) {
							currentWord = currentWord + String.valueOf(currentChar);
							currentChar = getNextChar();
						}
						tokenSeq.put(new String("CONST"), currentWord);
						currentWord = "";
						return;
					}
					else {
						Error(currentRow, "floating-point number error");
						currentWord = "";
						return;
					}
				}
				else {
					tokenSeq.put(new String("CONST"), currentWord);
					currentWord = "";
					return;
				}
			}
			else {
				Error(currentRow, "floating-point number error");
				currentWord = "";
				return;
			}
		}
		else {
			tokenSeq.put(new String("CONST"), currentWord);
			currentWord = "";
			return;
		}
	}
	
	/**
	 * 处理加号的函数
	 */
	private static void plusHandler() {
		currentWord = "";
		currentWord = currentWord + String.valueOf(currentChar);
		currentChar = getNextChar();
		if (currentChar == '+') {
			tokenSeq.put(new String("INC"), null);
			currentChar = getNextChar();
			return;
		}
		else {
			tokenSeq.put(new String("PLUS"), null);
			return;
		}
	}
	
	/**
	 * 处理减号的函数
	 */
	private static void minusHandler() {
		currentWord = "";
		currentWord = currentWord + String.valueOf(currentChar);
		currentChar = getNextChar();
		if (currentChar == '-') {
			tokenSeq.put(new String("DEC"), null);
			currentChar = getNextChar();
			currentWord = "";
			return;
		}
		else {
			tokenSeq.put(new String("MINUS"), null);
			currentWord = "";
			return;
		}
	}
	
	/**
	 * 不等号处理函数
	 */
	private static void neqHandler() {
		currentWord = "";
		currentWord = currentWord + String.valueOf(currentChar);
		currentChar = getNextChar();
		if (currentChar == '=') {
			tokenSeq.put(new String("NE"), null);
			currentChar = getNextChar();
			currentWord = "";
			return;
		}
		else {
			Error(currentRow, "not equal symbol error");
			return;
		}
	}
	
	/**
	 * 斜杠处理函数
	 * 可能是除号也可能是注释
	 */
	private static void slashHandler() {
		currentWord = "";
		currentWord = currentWord + String.valueOf(currentChar);
		currentChar = getNextChar();
		if (currentChar == '*') {
			currentWord = currentWord + String.valueOf(currentChar);
			currentChar = getNextChar();
			while (currentChar != '/') {
				currentWord = currentWord + String.valueOf(currentChar);
				currentChar = getNextChar();
				if (currentChar == '\0') {
					Error(currentRow, "annotation not matched");
					currentWord = "";
					return;
				}
			}
			currentChar = getNextChar();
		}
		else if ((String.valueOf(currentChar)).matches("[0-9]|([a-z]|[A-Z])")) {
			tokenSeq.put(new String("DIV"), null);
			currentWord = "";
			return;
		}
		else {
			Error(currentRow, "arithmatic error");
			currentWord = "";
			return;
		}
	}
	
	/**
	 * 等号处理函数
	 * 要么为等值判断，要么为赋值语句
	 */
	private static void equalHandler() {
		currentWord = "";
		currentWord = currentWord + String.valueOf(currentChar);
		currentChar = getNextChar();
		while (String.valueOf(currentChar).equals(" ")) {
			currentChar = getNextChar();
		}
		if (currentChar == '=') {
			currentWord = currentWord + String.valueOf(currentChar);
			currentChar = getNextChar();
			tokenSeq.put(new String("EQ"), null);
			currentWord = "";
			return;
		}
		else if ((String.valueOf(currentChar)).matches("^[0-9a-zA-Z_]{1,}$")) {
			tokenSeq.put(new String("ASS"), null);
			currentWord = "";
			return;
		}
		else {
			Error(currentRow, "arithmatic error");
			currentWord = "";
			return;
		}
	}
	
	private static void sort() {
		currentChar = code.get(0).charAt(0);
		while (currentChar != '\0') {
			switch (currentChar) {
			case ' ' :
				currentChar = getNextChar();
				break;
			case '+' :
				plusHandler();
				break;
			case '-' :
				minusHandler();
				break;
			case '(' :
				tokenSeq.put(new String("SLP"), null);
				currentChar = getNextChar();
				break;
			case ')' :
				tokenSeq.put(new String("SRP"), null);
				currentChar = getNextChar();
				break;
			case ';' :
				tokenSeq.put(new String("SEMI"), null);
				currentChar = getNextChar();
				break;
			case '{' :
				tokenSeq.put(new String("LP"), null);
				currentChar = getNextChar();
				break;
			case '}' :
				tokenSeq.put(new String("RP"), null);
				currentChar = getNextChar();
				break;
			case '*' :
				tokenSeq.put(new String("MUL"), null);
				currentChar = getNextChar();
				break;
			case '%' :
				tokenSeq.put(new String("PERC"), null);
				currentChar = getNextChar();
				break;
			case '/' :
				slashHandler();
				break;
			case '!' :
				neqHandler();
				break;
			case '=' :
				equalHandler();
				break;
			default:
				if ((String.valueOf(currentChar)).matches("[0-9]")) {
					numberHandler();
					break;
				}
				else if ((String.valueOf(currentChar)).matches("[a-z]|[A-Z]")) {
					identifierHandler();
					break;
				}
				else {
					Error(currentRow, "character not recognized");
					break;
				}
			}
		}
		return;
	}
	
	/**
	 * 打印错误信息
	 * @param rownumber错误所在行号
	 * @param errorinfo错误信息
	 */
	private static void Error(int rownumber, String errorinfo) {
		System.out.println("ERROR: " + errorinfo + ", in row " + rownumber);
	}
	
	/**
	 * 打印最终的token序列和符号表的结果
	 */
	private static void printResult() {
		System.out.println("Token Sequence:");
		Iterator<Entry<String, String>> iter = tokenSeq.entrySet().iterator();
		while (iter.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			System.out.println("<" + key + ", " + value + ">");
		}
		System.out.println();
		System.out.println("Symbol Table: \n" + symbolTable);
	}
	
	public static void main(String[] args) {
		readTestFileToArrList("C:\\Users\\liam\\Desktop\\compile_principle\\lab\\data\\lexicalAnalysisTestFile.txt");
		System.out.println(code);
		System.out.println();
		sort();
		printResult();
	}

}
