package runtju.christian.geomagic.exceptions.inputfile;

public class UnreadableInputFileException extends InputFileException {
    public UnreadableInputFileException() {
        super("Error reading input file");
    }
}
