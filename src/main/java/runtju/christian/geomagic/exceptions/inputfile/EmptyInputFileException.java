package runtju.christian.geomagic.exceptions.inputfile;

public class EmptyInputFileException extends InputFileException {
    public EmptyInputFileException() {
        super("Input file is empty");
    }
}
