import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class Main {
    private static final String ADD = "add";
    private static final String SUB = "sub";
    private static final String MUL = "mul";
    private static final String DIV = "div";
    private static final String POP = "pop";
    private static final String DUP = "dup";
    private static final String EXCH = "exch";
    private static final String EQ = "eq";

    private static final String PSTACK = "pstack";
    private static final String DEF = "def";
    private static Map<String, Double> symbolTable = new HashMap<>();
    private static PrintWriter logWriter;
    private static Stack<String> operationStack;

    public static void main(String[] args) {
        setupLogger();
        operationStack = new Stack<>();

        Scanner input = new Scanner(System.in);
        boolean ciclo = true;

        while (ciclo) {
            Stack<Double> elements = new Stack<>();
            ArrayList<String> printStack = new ArrayList<>();
            System.out.println("Agrega una expresión en postscript: o 'quit' para terminar. ");
            String expresion = input.nextLine();

            if (expresion.equals("quit")) {
                System.exit(0);
            }

            if (expresion.charAt(0)=='/'){
               try {
                   defList(expresion, symbolTable);
                   continue;
               }catch (IllegalArgumentException e){
                   String errorMessage = e.getMessage();
                   String specificError = "Expresión no válida: " + expresion; // Puedes personalizar el mensaje específico aquí
                   logError(errorMessage, specificError);
               }
           }

            try {
                evaluateExpression(elements, printStack, expresion);
            } catch (IllegalArgumentException e) {
                String errorMessage = e.getMessage();
                String specificError = "Expresión no válida: " + expresion; // Puedes personalizar el mensaje específico aquí
                logError(errorMessage, specificError);
            }
        }
    }

    private static void evaluateExpression(Stack<Double> elements, ArrayList<String> printStack, String expression) {
        String[] symbols = expression.split("\\s+");

        try { // Mover el bloque try-catch aquí para capturar excepciones en el método

            for (String symbol : symbols) {
                double result; // Declarar result una vez aquí

                if (symbolTable.containsKey(symbol)) {
                    double valor = symbolTable.get(symbol);
                    elements.push(valor);
                    printStack.add(symbol);
                } if (symbol.equals(ADD) || symbol.equals(SUB) || symbol.equals(MUL) || symbol.equals(DIV)) {
                    if (elements.size() < 2) {
                        throw new IllegalArgumentException("Faltan operadores para la operación");
                    }
                    double secondOP = elements.pop();
                    double firstOP = elements.pop();
                    switch (symbol) {
                        case ADD:
                            printStack.add(symbol);
                            result = firstOP + secondOP; // Asignar valor a result
                            elements.push(result);
                            break;
                        case SUB:
                            printStack.add(symbol);
                            result = firstOP - secondOP; // Asignar valor a result
                            elements.push(result);
                            break;
                        case MUL:
                            printStack.add(symbol);
                            result = firstOP * secondOP; // Asignar valor a result
                            elements.push(result);
                            break;
                        case DIV:
                            printStack.add(symbol);
                            if (secondOP == 0) {
                                throw new IllegalArgumentException("División por cero");
                            }
                            result = firstOP / secondOP; // Asignar valor a result
                            elements.push(result);
                            break;
                        default:
                            throw new IllegalArgumentException("Operador no reconocido");
                    }
                } else if (symbol.equals(DUP)) {
                    if (elements.size() < 1) {
                        throw new IllegalArgumentException("Faltan operandos para duplicar");
                    }
                    double valor = elements.peek();
                    elements.push(valor);
                } else if (symbol.equals(EXCH)) {
                    if (elements.size() < 2) {
                        throw new IllegalArgumentException("Faltan operandos para intercambiar");
                    }
                    double value2 = elements.pop();
                    double value1 = elements.pop();
                    elements.push(value2);
                    elements.push(value1);
                } else if (symbol.equals(EQ)) {
                    if (elements.size() < 2) {
                        throw new IllegalArgumentException("Faltan operandos para comparar igualdad");
                    }
                    double value1 = elements.pop();
                    double value2 = elements.pop();

                    if (value1 == value2) {
                        System.out.println(value1 + " es igual a " + value2);
                    } else {
                        System.out.println(value1 + " no es igual a " + value2);
                    }
                    elements.push(value2);
                    elements.push(value1);
                } else if (symbol.equals(PSTACK)) {
                    System.out.print("Contenido de la pila: " + elements);
                    System.out.println();
                } else if (symbol.equals(POP)) {
                    if (elements.size() < 1) {
                        throw new IllegalArgumentException("Faltan operandos para realizar pop");
                    }
                    elements.pop();
                    int index = printStack.size() - 1;
                    printStack.remove(index);
                } else if (esNumero(symbol)) {
                    elements.push(Double.parseDouble(symbol));
                    printStack.add(symbol);
                }

                operationStack.push(symbol);
            }

            if (printStack.contains(PSTACK) && elements.size() >= 1) {
                System.out.print("El resultado de la expresión ");
                printStack.forEach(elemento -> System.out.print(elemento + " "));
                System.out.println(" es: " + elements.peek());
                System.out.println();
                printStack.clear();
                elements.clear();
            } else if (!printStack.contains(PSTACK) && elements.size() >= 1) {
                System.out.println("El resultado de la expresión es: " + elements.peek());
                System.out.println();
                printStack.clear();
                elements.clear();
            } else {
                throw new IllegalArgumentException("Expresión no reconocible");
            }

        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage();
            String specificError = "Expresión no válida: " + expression; // Puedes personalizar el mensaje específico aquí
            logError(errorMessage, specificError);
        }
    }



    private static void setupLogger() {
        try {
            logWriter = new PrintWriter(new FileWriter("postscript_errores.log"));
        } catch (IOException e) {
            System.err.println("Error al configurar el archivo de registro: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void defList(String expresion, Map<String, Double> symbolTable){
        String expresion2 = expresion.substring(1);
        String[] expresionDef = expresion2.split("\\s+");
        String key = null;
        double value = 0;
        try{
            if (expresionDef.length==3 && expresionDef[2].equals("def") && esNumero(expresionDef[1])){
               key = expresionDef[0];
               value=Double.parseDouble(expresionDef[1]);
                System.out.println("Se guardo la informacion");
            } else throw new IllegalArgumentException("hay elementos que no se reconocen para el registro");
            symbolTable.put(key,value);
        }catch (IllegalArgumentException e){
            String errorMessage = e.getMessage();
            String specificError = "Expresión no válida: " + expresion; // Puedes personalizar el mensaje específico aquí
            logError(errorMessage, specificError);
        }
    }
    public static boolean esNumero(String texto) {
        try {
            Double.parseDouble(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void logError(String errorMessage, String specificError) {
        System.err.println(errorMessage);
        System.err.println("Error específico: " + specificError);
        logWriter.println("Error: " + errorMessage);
        logWriter.println("Error específico: " + specificError);
        logWriter.flush();
    }
}
