package main;

public class Vector {
  public final double x;
  public final double y;

  public static Vector between(Vector source, Vector target) {
    return new Vector(target.x - source.x, target.y - source.y);
  }

  public Vector(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Vector getUnitVector() {
    return divide(getLength());
  }

  public double getLength() {
    return Math.sqrt(x * x + y * y);
  }

  public Vector add(Vector other) {
    return new Vector(this.x + other.x, this.y + other.y);
  }

  public double dot(Vector other) {
    return this.x * other.x + this.y * other.y;
  }

  public Vector scale(double scalar) {
    return new Vector(x * scalar, y * scalar);
  }

  public Vector divide(double scalar) {
    return new Vector(this.x / scalar, this.y / scalar);
  }

  public Vector negate() {
    return new Vector(-x, -y);
  }

  public Vector turn(double radians) {
    return new Vector(
        x * Math.cos(radians) - y * Math.sin(radians),
        x * Math.sin(radians) + y * Math.cos(radians));
  }
}
