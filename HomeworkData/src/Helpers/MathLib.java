package Helpers;
public class MathLib {	
	public static double calculateThetaFromPercent(double percentage) {
		return ((percentage) * 2 * Math.PI) + Math.PI / 2.0;
	}
	
	public static double toRadians(double theta) {
		return (theta / 360.0) * 2 * Math.PI;
	}
	
	public static double toDegrees(double theta) {
		return (theta / (2 * Math.PI)) * 360.0;
	}
	
	// vector1 and vector2 must have the same number of components
	public static double dotProduct(double[] vector1, double[] vector2) {
		double result = 0;
		for (int i = 0; i < vector1.length; i++) {
			result += vector1[i] * vector2[i];
		}
		return result;
	}
	
	public static double magnitude(double[] vector) {
		double sqrtInside = 0;
		for (int i = 0; i < vector.length; i++) {
			sqrtInside += vector[i] * vector[i];
		}
		return Math.sqrt(sqrtInside);
	}
	
	public static double calculateTheta(double x, double y, double radius) {
		double[] startingVector = {0.0, radius};
		double[] pointVector = {x, y};
		double dotProduct = MathLib.dotProduct(startingVector, pointVector);
		double magnitudeProduct = MathLib.magnitude(startingVector) * MathLib.magnitude(pointVector);
		
		double result = MathLib.toDegrees(Math.acos(dotProduct / magnitudeProduct));
	
		if (x > 0)
			result = (180 - result) + 180;
		
		return result;
	}
}
