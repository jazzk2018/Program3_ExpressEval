package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

    public static String delims = " \t*+-/()[]";
            
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        StringTokenizer strExpr = new StringTokenizer(expr, delims); // Breaks expr into tokens: a*b+A[b] -> a,b,A,b
        String ptrToken = "";
        while (strExpr.hasMoreTokens()){
            ptrToken = strExpr.nextToken();
            int ptrIndex = expr.indexOf(ptrToken), ptrLength = ptrToken.length(); 
            // IF: the current token beings with a letter, meaning it's a variable, then DO
            // EX: varA -> begins with a letter, not a number
            if (Character.isLetter(ptrToken.charAt(0))) {
                // IF the token is the last character in the expr, then it must be variable
                // because no [ after it
                if (ptrIndex + ptrLength > expr.length() - 1) {
                    Variable newVar = new Variable(ptrToken);
                    if (!vars.contains(newVar)) vars.add(newVar);
                }
                // The character after token is '[', meaning token is an array
                else if (expr.charAt(ptrIndex + ptrLength) == '[') {
                    Array newArray = new Array(ptrToken);
                    if (!arrays.contains(newArray)) arrays.add(newArray);
                }
                // If token is not an array, it must be a variable
                else {
                    Variable newVar = new Variable(ptrToken);
                    if (!vars.contains(newVar)) vars.add(newVar);
                }
            }
        }
        //System.out.println(vars);
        //System.out.println(arrays);
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
                continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
                arr = arrays.get(arri);
                arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays){
        Stack<Float> numStack = new Stack<Float>(); //Create a stack for numbers
        Stack<String> operatorStack = new Stack<String>(); //Create a stack for operators

        //Iterate through the Expression (let’s say VARIABLE A here)
        for(int a = 0; a < expr.length(); a++){ 
            //Check if the character at index VAR A in Expression is a digit (<— THIS IS TO ADD NUMBERS TO THE STACK FOR NUMBERS)
            if(Character.isDigit(expr.charAt(a))){
                int tempInt = a; //Make a dummy variable (VAR B) and set it equals to VAR A.
                //VAR P is need to substring the Expression to add onto the stack for numbers. The number is from A —> B
                while(Character.isDigit(expr.charAt(tempInt))) {
                    //Iterate through the Expression using VAR B
                    tempInt++;
                    //Break the loop before VAR B ever exceeds the Expression length
                    if(tempInt == expr.length()) break; 
                }
                //Substring Expression using VAR A and VAR B <- This creates a #
                numStack.push(Float.parseFloat(expr.substring(a,tempInt))); //Add the substring to the stack for #s
                a = tempInt-1; //Manipulate VAR A so that it doesn’t iterate through the same characters of Expression
            } else if(expr.charAt(a)=='['){
                //Check if character at index VAR A in Expression is an array
                int tempInt = a-1; //dummy Var behind A to keep track of which Array to access
                while(Character.isLetter(expr.charAt(tempInt))){ //Finding the Array name
                    tempInt--; //K is always going to be behind charAt(a), so you have to go backwards from the bracket
                    if(tempInt==-1) break; //In case Array is the first thing in expression.
                }
                for(int d=0; d<arrays.size(); d++){
                    // Iterate through Array arrays using D
                    if(arrays.get(d).name.equals(expr.substring(tempInt+1, a))){
                        //Check whether or not the specific iteration is equal to the Array substring. 
                        //Checking if index D in ArrayList arrays is the same as the one we just found.
                        int [] arr = arrays.get(d).values; 
                        //Create dummy array to find the array values at index D
                        numStack.push((float)arr[(int)evaluate(expr.substring(a+1), vars, arrays)]);
                        //Use recusion to EVALUATE what's after the opening bracket; MAKE SURE YOU FIND THE VALUE FROM THE DUMMY ARRAY
                        //Push/Add the newfound variable as an array onto numStack Stacks
                        break;
                    }
                }

                int bool = 0; //create an int variable to make sure there are equal amounts of opening and closing brackets
                boolean bypass=false; //bypass is automatically set to false, to assume that there are arrays
                for(int i = a+1; i<expr.length(); i++) {
                //Iterate through the expression again, but this time start at a position in front of a; 
                //Set Variable I equals to 1 ahead of A
                    if(expr.charAt(i)==']' && bool==0){ //Check for closing brackets
                        a = i;
                        //If A isn't the last character in the expression, set bypass equals to true
                        if(!(a == expr.length()-1)) bypass = true; 
                        break;
                    }
                    else if(expr.charAt(i)=='[') bool++;
                    else if(expr.charAt(i)==']') bool--;
                    
                }
                //Continue the loop if bypass is true
                if(bypass) continue;
            }
            else if(Character.isLetter(expr.charAt(a))){ //Check if character at iteration of A is a letter (i.e. a variable)
                int h = a; //Make a dummy of variable A, yet again. This time we'll use H.
                while(Character.isLetter(expr.charAt(h))) //Iterate through expression using H 
                {
                    h++; //Increase H by 1 before you do anything in case H is the entirerty of the expression.
                    if(h==expr.length()) //Check if H is the entirety of the expression
                    {
                        break;
                    }
                }
                //Check whether or not H is just full of variables
                if(h==expr.length()){
                    for(int c = 0; c < vars.size(); c++){ 
                        //Iterate through ArrayList variables to check whether or not there are variables 
                        if(vars.get(c).name.equals(expr.substring(a,h))){
                            //If there are variables, add the value of that variable onto the numStack Stack
                            numStack.push((float)vars.get(c).value); //Adding onto the stack
                            break;
                        }
                    }
                    //Reduce the size of A since you are removing that amount of variables
                    a = h-1; 
                }
                //Check if H is NOT an opening Array bracket
                else if(!(expr.charAt(h)=='[')){
                    for(int c=0;c<vars.size();c++){
                        //Iterate through ArrayList vars, using variable C
                        if(vars.get(c).name.equals(expr.substring(a,h))){
                            //Check if vars at index C is equal to substringed expression (which is a variable) between A and H
                            numStack.push((float)vars.get(c).value); //Adding/Pushing vars (index C) to numStack Stack
                            break;
                        }
                    }
                    a = h-1; //Reduce the size of A since you are removing that amount of variables
                }
            }
            else if(expr.charAt(a)=='(') //Time to check for parantheses; Check for an opening parantheses 
            {
                //Since it's a parantheses, use recursion to send back the expression after the parantheses to evalute (INCLUDE VARS + ARRAYS)
                //Should look like --> StackOfNumbers.push((float)evaluate(expr.substring(EVERYTHING AFTER PARANTHESES), vars, arrays));
                numStack.push((float)evaluate(expr.substring(a+1), vars, arrays)); 

                //Run the same bypass system as before, same thing you do to check through arrays, you do here. Check the balance!!!
                int bool=0; 
                boolean bypass=false; 
                for(int i=a+1;i<expr.length();i++){
                    if(expr.charAt(i)==')' && bool==0){
                        a=i;
                        if(!(a==expr.length()-1)) bypass=true;
                        break;
                    }
                    else if(expr.charAt(i)=='(') bool++;
                    
                    else if(expr.charAt(i)==')') bool--;
                }
                if(bypass) continue;
            }
            
            //Check if the character at A is an operator -> transfer it to the operatorStack
            else if(expr.charAt(a)=='+') operatorStack.push("+");
            
            else if(expr.charAt(a)=='-') operatorStack.push("-");
            
            else if(expr.charAt(a)=='*') operatorStack.push("*");
            
            else if(expr.charAt(a)=='/') operatorStack.push("/");

            if(expr.charAt(a)==')' || a == expr.length()-1 || expr.charAt(a)==']') {
            //Check for ending parantheses/brackets or if nearing the end of expression
                if(!(operatorStack.isEmpty())) //Check if there's operator stacks, otherwise there's a big issue
                {
                Stack<String> newOpStack = new Stack<String>(); //Create new stacks for operators
                Stack<Float> newNumStack = new Stack<Float>(); //Create new stacks for numbers
                while(!(numStack.isEmpty())) //Iterate through numStack and basically reverse the order into the new stack for numbers
                {
                    newNumStack.push(numStack.pop());
                }
                
                //Iterate through operatorStack and reverse the order into the new stack for operators
                while(!(operatorStack.isEmpty())) {
                    newOpStack.push(operatorStack.pop());
                }
                
                //Iterate through the new stack of operators
                while(!(newOpStack.isEmpty())) {
                    //Peek forward to the opeartor and check each individual operator
                    if(newOpStack.peek().equals("+")) {
                        operatorStack.push(newOpStack.pop()); //Pop the stack off the new opStack and push it back on to the old opStack
                        numStack.push(newNumStack.pop()); //Do the same exact thing for the new numStack and the old numStack
                        
                        //check if there are no more new opStacks and if there's still more new numStack items
                        //If there are more new numStack items, just keep adding them to the old numStack;
                        if(newOpStack.isEmpty() && !(newNumStack.isEmpty())) numStack.push(newNumStack.pop());
                    }
                    else if(newOpStack.peek().equals("-")) {
                        operatorStack.push(newOpStack.pop()); //Same process as add;
                        numStack.push(newNumStack.pop()); //Same process as add;
                        if(newOpStack.isEmpty() && !(newNumStack.isEmpty())) //Same process as add
                        numStack.push(newNumStack.pop());
                    }
                    else if(newOpStack.peek().equals("*") || newOpStack.peek().equals("/")) 
                    {
                        //Create a float (say value) and set it equal to the first pop of the newNumStack;
                        //This is to find the dividend and the multiplicand (as you need the ones first, then tens, and so on)
                        float val=newNumStack.pop(); 
                        
                        //To make life easier, make a while loop for multiply and divide here. 
                        //For every iteration of the OG while loop, this will check for >1 multiples or divides.
                        while(newOpStack.peek().equals("*") || newOpStack.peek().equals("/")) {
                            
                            if(newOpStack.peek().equals("*")) val=val*newNumStack.pop();
                            //if it's a multiply operator, do value *= the float you made before
                            
                            else if(newOpStack.peek().equals("/")) val=val/newNumStack.pop(); 
                            //if it's a divide operator, do value /= the float
                            
                            newOpStack.pop();
                            if(newOpStack.isEmpty()) break; //If the new operatorStack is empty, break
                        }
                        newNumStack.push(val); //REMEMBER --> Push the value variable onto the new numStack
                    }
                }
                while(!(numStack.isEmpty())) {
                    //If old numStack isn't empty, push the rest of the items onto the new numStack
                    newNumStack.push(numStack.pop());
                }
                while(!(operatorStack.isEmpty())) {
                    //Same thing for old operatorStack
                    newOpStack.push(operatorStack.pop());
                }
                
                float val=newNumStack.pop(); //Create another float variable equals to the first item of the new numStack
                while(!(newOpStack.isEmpty())) {
                    //Iterate through the new operatorStack until it's empty. Check if the operator is +/-
                    String op=newOpStack.pop(); //Make a string variable and set it equal to the pop off the new operatorStack
                    if(op.equals("+")) val=val+newNumStack.pop();
                    //If the str variable is add, do value = value + the pop off the new numStack
                
                    if(op.equals("-")) val = val-newNumStack.pop();
                    //If the str variable is subtract, do the same thing for subtract.
                
                }
                return val; //Return the value variable once all is done.
                }
                else return numStack.pop();
            }
        }
        return 0;
    }
}