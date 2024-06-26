package Lenguajes_Automatas2.Gecko;

import Utilidades.LexicalUtility;
import Utilidades.Lista;
import Utilidades.myToken;

public class LexicalGecko extends LexicalUtility {

	public static void main(String[] args) throws Exception {
		LexicalGecko lexical = new LexicalGecko("src\\Lenguajes_Automatas2\\txt\\Gecko3.txt");
		lexical.printInput();
		lexical.printTokenTable();
		lexical.printSymbolTable();
	}

	// Variables globales para un facil acceso
	private final String input; // Cadena a analizar
	private int charPos; // Posicion del char actual
	private int row; // Valor entero de la linea actual
	private int col; // Valor entero de la columna actual

	// Aqui se guardan las palabras reservadas
	private final Lista<String> keyWords = new Lista<>(BEGIN, END, IF, ELSE, INTEGER, FLOAT, STRING, CHAR, BOOL, FOR,
			WHILE, DO, SWITCH, TRUE, FALSE, CONTINUE, RETURN, NEW, THIS, NULL, CASE, DEFAULT, BREAK);

	// Aqui se guardan los posibles simbolos a leer
	private final Lista<Character> symbolsList = new Lista<>('=', '<', '>', '(', ')', '{', '}', '[', ']', ':', '"',
			'\'', ';', '?'); // Aqui van los simbolos

	// Aqui van los operadores arimeticos
	private final Lista<Character> artmieticOperator = new Lista<>('+', '-', '*', '/', '^', '%');

	// Aqui van los operadores logicos
	private final Lista<Character> logicOperator = new Lista<>('!', '&', '|');

	// Constructor de la clase que recibe como parametro la direccion del archivo de
	// texto
	public LexicalGecko(String path) throws Exception {
		this.input = deleteComments(getTextFile(path));
		this.charPos = 0;
		this.row = 1;
		this.col = 1;
		tokenize();
		getSymbolTable(tokenTable);
	}

	// Metodo para generar la lista de tokens
	public Lista<myToken> tokenize() {
		while (charPos < input.length()) { // Analiza caracter por caracter hasta que no haya mas elementos
			char current = peekChar(); // Guarda el caracter actual
			if (Character.isWhitespace(current)) { // Si el caracter actual es un espacio en blanco
				if (current == '\n') { // Si el caracter actual es salto de linea
					row++; // Incrementa la linea en 1
					col = 1; // Reinicia el contador de columnas
					popChar(); // Consume el char si hacer nada mas
				} else {
					popChar(); // Consume el char si hacer nada mas
				}
			} else if (Character.isDigit(current)) { // Si el caracter actual es un digito
				tokenTable.addToEnd(tokenizeNumberOrReal()); // Llama la funcion para tokenizar el numero
				isOverflow();
			} else if (Character.isLetter(current)) { // Si el caracter actual es una letra
				tokenTable.addToEnd(tokenizeIdentifierOrKeyword()); // Verifica si es un Id
				isOverflow(); 
			} else if (isSymbol(current)) { // Si el caracter actual es un simbolo
				tokenTable.addToEnd(tokenizeOperatorOrSymbol()); // Se llama la funcion que verifica si es un operador o
				isOverflow(); // es un symbolo
			} else { // Si ninguna de las anteriores se cumplio
				throw new RuntimeException("Unknown character: " + current); // Finaliza el programa porque el
																				// caracter no pudo ser identificado
			}
		}
		isOverflow();
		return tokenTable;
	}

	private myToken tokenizeOperatorOrSymbol() {
		char current = popChar();
		if ("=<>!".indexOf(current) != -1 && peekChar() == '=') { // Operadores de comparacion
			String value = current + String.valueOf(popChar());
			return new myToken(value, value, row, null, col++);

		} else if (isArtmieticOperator(current) && peekChar() == '=') { // Posibles metodos de asginacion compuesta
			String value = current + String.valueOf(popChar());
			return new myToken(value, value, row, null, col++);

		} else if ("+-&|".indexOf(current) != -1 && current == peekChar()) { // Posible incremento u operador logico
			String value = current + String.valueOf(popChar());
			return new myToken(value, value, row, null, col++);

		} else if (current == '"') {
			return getString(current);
		} else if (current == '\'') {
			return getChar(current);
		} else {
			return new myToken(current + "", current + "", row, null, col++); // Cualquiero otro simbolo simple de la
																				// lista
		}
	}

	private myToken tokenizeIdentifierOrKeyword() {
		StringBuilder identifier = new StringBuilder();
		while (Character.isLetterOrDigit(peekChar()) || peekChar() == '_') {
			identifier.append(popChar());
		}
		String value = identifier.toString();
		if (isKeyword(value)) {
			return new myToken(value, value, row, null, col++);
		} else {
			return new myToken(IDENTIFIER, value, row, null, col++);
		}
	}

	private myToken tokenizeNumberOrReal() {
		StringBuilder number = new StringBuilder();
		while (Character.isDigit(peekChar())) {
			number.append(popChar());
		}
		if (peekChar() == '.') {
			number.append(popChar());
			while (Character.isDigit(peekChar())) {
				number.append(popChar());
			}
			return new myToken(REAL, number.toString(), row, String.valueOf(number), col++);
		} else {
			return new myToken(NUMBER, number.toString(), row, String.valueOf(number), col++);
		}
	}

	private myToken getString(char currentChar) {
		String value = ""+currentChar;
		while (peekChar() != '"') {
			value+=popChar();
		}
		value+=popChar();
		return new myToken(TEXT, value, row, value, col++);
	}

	private myToken getChar(char currentChar) {
		String value = ""+currentChar;
		while (peekChar() != '\'') {
			value+=popChar();
		}
		value+=popChar();
		return new myToken(CHARACTER, value, row, value, col++);
	}

	// Metodos para la obtencion de caracteres
	private char popChar() {
		return input.charAt(charPos++);
	}

	private char peekChar() {
		return charPos < input.length() ? input.charAt(charPos) : '\0';
	}

	// Metodos booleanos
	private boolean isKeyword(String value) {
		return keyWords.contains(value);
	}

	private boolean isSymbol(char value) {
		return symbolsList.contains(value) || isLogicOperator(value) || isArtmieticOperator(value);
	}

	private boolean isLogicOperator(char value) {
		return logicOperator.contains(value);
	}

	private boolean isArtmieticOperator(char value) {
		return artmieticOperator.contains(value);
	}
	// Otros metodos
	public void printTokenTable() {
		System.out.println("\n--------------------Tabla de tokens--------------------");
		printTable(tokenTable);
	}

	public void printSymbolTable() {
		System.out.println("\n--------------------Tabla de simbolos--------------------");
		printTable(symbolTable);
	}

	public Lista<myToken> getSymbolTable() {
		return symbolTable;
	}

	public Lista<myToken> getTokenTable() {
		return tokenTable;
	}

	public void printInput() {
		printInput(this.input);
	}

	private void isOverflow() { //Verifica que no se haya excedido la capasidad maxima de tokens
		if (tokenTable.getSize() > MAX) {
			throw new OutOfMemoryError("Capacidad de tokens excedida: MAX->" + MAX);
		}
	}
}
