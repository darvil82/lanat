package argparser;

public record ArgValueCount(byte min, byte max) {
	public ArgValueCount(int min, int max) {
		this((byte)max, (byte)max);
	}
	public ArgValueCount(int max) {
		this(max, max);
	}
}