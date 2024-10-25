package runtju.christian.geomagic.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import runtju.christian.geomagic.dto.Line2D;
import runtju.christian.geomagic.exceptions.ImageCreationException;
import runtju.christian.geomagic.exceptions.InputException;
import runtju.christian.geomagic.exceptions.inputfile.InputFileException;
import runtju.christian.geomagic.exceptions.inputfile.UnreadableInputFileException;
import runtju.christian.geomagic.service.ChainService;
import runtju.christian.geomagic.service.ImageService;
import runtju.christian.geomagic.service.LineService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired)) //lombok magic to avoid constructor boilerplate
public class ChainImageController {
    private final ChainService chainService;
    private final ImageService imageService;
    private final LineService lineService;


    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody byte[] imageFromStringInput(@RequestBody String lineInput) throws ImageCreationException, InputFileException {
        try {
            var lines = lineService.buildLinesFromString(lineInput);
            return imageFromJsonInput(lines);
        } catch (InputException e) {
            throw new InputFileException("File contains invalid input");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody byte[] imageFromJsonInput(@RequestBody List<Line2D> lines) throws ImageCreationException {
        var chains = chainService.buildChains(lines);
        return imageService.buildImageFromChains(chains);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody byte[] imageFromFileInput(@RequestPart("file") MultipartFile file) throws ImageCreationException, InputFileException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            var sb = new StringBuilder();
            reader.lines().forEach(line -> sb.append(line).append("\n"));
            return imageFromStringInput(sb.toString());
        } catch (IOException e) {
            throw new UnreadableInputFileException();
        }
    }
}
