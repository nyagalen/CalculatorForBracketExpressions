package com.javarush.task.task34.task3404;




import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* 
Рекурсия для мат. выражения
*/

public class Solution {
    public static void main(String[] args) {
        Solution solution = new Solution();
   

        solution.recurse("sin(45) - cos(45)", 0);


    }

    public void recurse(final String expression, int countOperation) {
       // System.out.println(expression + " " + countOperation);
        if(countOperation==0) {
            Pattern allOperationsPattern = Pattern.compile("(sin|cos|tan)|[+\\-^*/]");
            Matcher allOperationsMatcher = allOperationsPattern.matcher(expression);
            StringBuffer sb= new StringBuffer();
            while (allOperationsMatcher.find()){
              //  System.out.print(" operation "+allOperationsMatcher.group()+" position "+allOperationsMatcher.start() );
                countOperation++;
            }
            
        }

        String expressionNoSpaces = expression.replace(" ", "");




        Pattern sincosWhyNoBrackets = Pattern.compile(("(sin|cos|tan)-?\\d+(\\.\\d+)?"));
        Matcher sincosNoBracketsMatcher = sincosWhyNoBrackets.matcher(expressionNoSpaces);
        StringBuffer letsAddthoseBrackets = new StringBuffer();
        while (sincosNoBracketsMatcher.find()){
            String found = sincosNoBracketsMatcher.group();
            sincosNoBracketsMatcher.appendReplacement(letsAddthoseBrackets, found.substring(0,3)+"("+found.substring(3)+")");
        }
        sincosNoBracketsMatcher.appendTail(letsAddthoseBrackets);
        String bracketedSinCos = letsAddthoseBrackets.toString();

        Pattern sinCosPattern = Pattern.compile("(sin|cos|tan)\\(-?\\d+(\\.)?(\\d+)?\\)");
        Matcher sinCosMatcher = sinCosPattern.matcher(bracketedSinCos);
        StringBuffer sb = new StringBuffer();                          //здесь выдало --, образовавшийся от минуса перед отрицатеным результатом косинуса
        if (sinCosMatcher.find()) {
            sinCosMatcher.appendReplacement(sb,getSinCosTan(sinCosMatcher.group()) );
        }
        sinCosMatcher.appendTail(sb);

        String sinCosTanCalculated = sb.toString();

        String doubleMinusesReplaced = sinCosTanCalculated.replaceAll("--", "+");
        String plusMinusReplaced = doubleMinusesReplaced.replaceAll("-\\+|\\+-", "-");
        if(!plusMinusReplaced.equals(expressionNoSpaces)) {
            recurse(plusMinusReplaced, countOperation);
            return;
        }





        Pattern bracketsMightBeASoleBracketedNumberInside = Pattern.compile("(\\(([^()]+(\\(-?\\d+(\\.\\d+)?\\))[^()]*)+\\))|(\\(([^()]*(\\(-?\\d+(\\.\\d+)?\\))[^()]+)+\\))");
          Matcher bracketsAndNumberInBracketsMatcher = bracketsMightBeASoleBracketedNumberInside.matcher(plusMinusReplaced);
          StringBuffer bracketsBracketedNumbersb = new StringBuffer();
          if (bracketsAndNumberInBracketsMatcher.find())
              bracketsAndNumberInBracketsMatcher.appendReplacement(bracketsBracketedNumbersb, doExpressionNoSincosNoBrackets(bracketsAndNumberInBracketsMatcher.group()));
          bracketsAndNumberInBracketsMatcher.appendTail(bracketsBracketedNumbersb);
          String noMoreSoleNumbersInBrackets = bracketsBracketedNumbersb.toString();
          if(!noMoreSoleNumbersInBrackets.equals(plusMinusReplaced)) {
              recurse(noMoreSoleNumbersInBrackets, countOperation);
              return;
          }





        if(noMoreSoleNumbersInBrackets.matches("(-?\\d+((\\.|,)\\d+)?)|(\\(-?\\d+((\\.|,)\\d+)?\\))")){//выход из рекурсии, если у нас наконец-то нормальное число
            String returnable = noMoreSoleNumbersInBrackets;
            String returnableNoBrackets = null;
            if (returnable.contains("(")&&returnable.contains(")")) returnableNoBrackets = returnable.substring(1, returnable.length()-1);
            else returnableNoBrackets = returnable;
            double returnableDouble = Double.parseDouble(returnableNoBrackets);
            DecimalFormat df = new DecimalFormat("#.##");
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            df.setDecimalFormatSymbols(dfs);
            String output = df.format(returnableDouble);
            String outputNoMinusZero = null;
            if (output.equals("-0")) outputNoMinusZero = "0";
            else outputNoMinusZero = output;
            System.out.println(outputNoMinusZero+" "+ countOperation);
            return;

        }

        else if(!noMoreSoleNumbersInBrackets.contains("(")&&!noMoreSoleNumbersInBrackets.contains(")")||
        noMoreSoleNumbersInBrackets.matches("((((\\(-?\\d+(\\.\\d+)?\\))?[^()])+\\(-?\\d+(\\.\\d+)?\\)([^()](\\(-?\\d+(\\.\\d+)?\\))?)*)+)|((([^()](\\(-?\\d+(\\.\\d+)?\\))?)*\\(-?\\d+(\\.\\d+)?\\)((\\(-?\\d+(\\.\\d+)?\\))?[^()])+)+)")){

          String recursable = doExpressionNoSincosNoBrackets(noMoreSoleNumbersInBrackets);
            recurse(recursable, countOperation);
            return;

        } else{

            Pattern bracketsPattern = Pattern.compile("\\(-?\\d+(\\.\\d+)?([+*^/-]\\d+(\\.\\d+)?)+\\)");
            Matcher bracketsMatcher = bracketsPattern.matcher(noMoreSoleNumbersInBrackets);
            StringBuffer bracketsSb = new StringBuffer();
            if (bracketsMatcher.find()){
                bracketsMatcher.appendReplacement(bracketsSb, doExpressionNoSincosNoBrackets(bracketsMatcher.group()));
            }
            bracketsMatcher.appendTail(bracketsSb);
            String recursable = bracketsSb.toString();
            recurse(recursable, countOperation);
            return;
        }


    }
    public String doExpressionNoSincosNoBrackets(String sinCosTanCalculated){//надо придумать регулярку шоб говорила что учитывать минус перед числом
        //написала ему что число может быть отрицательным, если это начала строки или выражения в скобках
        Pattern powPattern = Pattern.compile("(((?<=^|\\()-?\\d+(\\.\\d+)?)|(\\d+(\\.\\d+)?)|(\\(-\\d+(\\.\\d+)?\\)))\\^((-?\\d+(\\.\\d+)?)|(\\(-\\d+(\\.\\d+)?\\)))");
        Matcher powMatcher = powPattern.matcher(sinCosTanCalculated);
        //
        StringBuffer powSb = new StringBuffer();//если сей минус в начале строки или после скобки

        if (powMatcher.find()) {

            powMatcher.appendReplacement(powSb, getOperationDone(powMatcher.group()));
        }
        powMatcher.appendTail(powSb);
        String powsCalculated = powSb.toString();
        if(powsCalculated.contains("^")) return powsCalculated;




    Pattern multiplyDividePattern = Pattern.compile("(((?<=^|\\()-?\\d+(\\.\\d+)?)|(\\d+(\\.\\d+)?)|(\\(-\\d+(\\.\\d+)?\\)))[*/]((-?\\d+(\\.\\d+)?)|(\\(-\\d+(\\.\\d+)?\\)))");//[*/]
    Matcher multMatcher = multiplyDividePattern.matcher(powsCalculated);
    StringBuffer multSb = new StringBuffer();
    if (multMatcher.find()) {
        multMatcher.appendReplacement(multSb, getOperationDone(multMatcher.group()));
    }
    multMatcher.appendTail(multSb);
  String  multCalculated = multSb.toString();
    if(multCalculated.contains("*")||multCalculated.contains("/")) return multCalculated;


        Pattern plusMinusPattern = Pattern.compile("(((?<=^|\\()-?\\d+(\\.\\d+)?)|(\\d+(\\.\\d+)?)|(\\(-\\d+(\\.\\d+)?\\)))[+-]((\\d+(\\.\\d+)?)|(\\(-\\d+(\\.\\d+)?\\)))");
        Matcher plusMinusMatcher = plusMinusPattern.matcher(multCalculated);
        StringBuffer plusSb = new StringBuffer();
        if (plusMinusMatcher.find())
            plusMinusMatcher.appendReplacement(plusSb, getOperationDone(plusMinusMatcher.group()));
        plusMinusMatcher.appendTail(plusSb);
        String plusCalculated = plusSb.toString();

        String plusCalculatedRemoveBrackets1=null;
        String plusCalculatedRemoveBrackets2 = null;

        if(plusCalculated.matches("\\(\\d+(\\.\\d+)?\\)")){//правильно, если число отрицательное, скобки не убираем
             plusCalculatedRemoveBrackets1 = plusCalculated.replace("(", "");
             plusCalculatedRemoveBrackets2= plusCalculatedRemoveBrackets1.replace(")","");
        }else plusCalculatedRemoveBrackets2=plusCalculated;//если оно ещё не досчиталось до нормального числа, скобки оставляем



        return plusCalculatedRemoveBrackets2;
    }

    public String getSinCosTan(String sinStr){
        int start = sinStr.indexOf("(");
        int end = sinStr.indexOf(")");
        double gradus = Double.parseDouble(sinStr.substring(start+1, end));
        double sinCosTanWeNeed = 0;
        switch (sinStr.substring(0,3)){
            case "sin":
               sinCosTanWeNeed= Math.sin(Math.toRadians(gradus));
               break;
            case "cos":
                sinCosTanWeNeed = Math.cos(Math.toRadians(gradus));
                break;
            case "tan":
                sinCosTanWeNeed = Math.tan(Math.toRadians(gradus));
                break;

        }

        StringBuilder sb = new StringBuilder();
        sb.append(sinCosTanWeNeed);
        return sb.toString();
    }
    public String getOperationDone(String multExpression){
        Pattern firstDoublePattern = Pattern.compile("(^-?\\d+(\\.)?\\d*)|(^\\(-\\d+(\\.\\d+)?\\))");
        Matcher firstDoubleMatcher = firstDoublePattern.matcher(multExpression);
        String firstDoubleStr = null;
        if (firstDoubleMatcher.find()) firstDoubleStr = firstDoubleMatcher.group();
        String firstDoubleStrNoBrackets = null;
        if(firstDoubleStr.contains("(")&&firstDoubleStr.contains(")")){
            firstDoubleStrNoBrackets=firstDoubleStr.substring(1, firstDoubleStr.length()-1);

        } else firstDoubleStrNoBrackets = firstDoubleStr;
        double firstDouble = Double.parseDouble(firstDoubleStrNoBrackets);
        Pattern secondDoublePattern = Pattern.compile("(\\d+(\\.)?(\\d)*$)|(\\(-\\d+(\\.\\d+)?\\)$)");
        Matcher secondDoubleMatcher = secondDoublePattern.matcher(multExpression);
        String secondDoubleString = null;
        if (secondDoubleMatcher.find()) secondDoubleString = secondDoubleMatcher.group();
        String secondDoubleStrNoBrackets = null;
        if(secondDoubleString.contains("(")&&secondDoubleString.contains(")")) secondDoubleStrNoBrackets=secondDoubleString.substring(1, secondDoubleString.length()-1);
        else secondDoubleStrNoBrackets = secondDoubleString;

        double secondDouble = Double.parseDouble(secondDoubleStrNoBrackets);

        Pattern minusPattern = Pattern.compile("(\\d-\\d)|(\\)-\\()|(\\d-\\()|(\\)-\\d)");
        Matcher minusMatcher = minusPattern.matcher(multExpression);
        double minus = 0;
        if(minusMatcher.find()) {
           // if(secondDouble<0) minus = firstDouble+secondDouble;
          minus = firstDouble - secondDouble;//мда, типа если у нас есть минусматчер, то там точно минус, минус точно один, значит что - можно плюсовать 2 числа?..

             



            String minusFormatted = String.format(Locale.US, "%f", minus);
           // String pleaseNoMinusZero = minusMightContainComma.replace("-0", "0");
            
            
           
            return minusFormatted;
        }

        Pattern operationPattern = Pattern.compile("[*/^+]");//ёлки-моталки он же может это найти и перед цифрой, а не между


        Matcher operationMatcher = operationPattern.matcher(multExpression);
        String operation=null;
        if(operationMatcher.find()) operation = operationMatcher.group();


        double multDouble = 0;
        switch (operation){
            case"*":
                multDouble = firstDouble*secondDouble;
                break;
            case "/":
                multDouble = firstDouble/secondDouble;
                break;
            case"+":
                multDouble=Double.sum(firstDouble, secondDouble);
                break;
            case"^":
                    multDouble = Math.pow(firstDouble,secondDouble);
                    break;




        }



        String formatted =   String.format(Locale.US, "%f", multDouble);
      //  String noMinusZero = doubleMightContainComma.replace("-0", "0");

        return formatted;


    }

    public Solution() {
        //don't delete
    }
}
