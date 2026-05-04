package rdf.mapping.functions;

public class StringConcat {

    public static String stringConcat(String leftPart, String rightPart) {
        //if(leftPart == null){
        //    System.out.println("Right part: " + rightPart);
        //}
        // if(!rightPart.isEmpty() && rightPart.equals("8451")) {
        //     System.out.println("Left part: " + leftPart);
        //     System.out.println("Right part: " + rightPart);
        // }
        if (leftPart == null || rightPart == null) {
            return null;
        }

        return leftPart + rightPart;
    }
}
