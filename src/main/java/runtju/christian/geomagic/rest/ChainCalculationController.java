package runtju.christian.geomagic.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import runtju.christian.geomagic.dto.Line2D;
import runtju.christian.geomagic.dto.SimplifiedLineChain;
import runtju.christian.geomagic.exceptions.InputException;
import runtju.christian.geomagic.exceptions.inputfile.EmptyInputFileException;
import runtju.christian.geomagic.exceptions.inputfile.InputFileException;
import runtju.christian.geomagic.exceptions.inputfile.UnreadableInputFileException;
import runtju.christian.geomagic.service.ChainService;
import runtju.christian.geomagic.service.LineService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/calculation", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired)) //lombok magic to avoid constructor boilerplate
public class ChainCalculationController {
    private final ChainService chainService;
    private final LineService lineService;


    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody List<SimplifiedLineChain> infoFromStringInput(@RequestBody String lineInput) throws InputFileException {
        try {
            var lines = lineService.buildLinesFromString(lineInput);
            return infoFromJsonInput(lines);
        } catch (InputException e) {
            throw new InputFileException("File contains invalid input");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<SimplifiedLineChain> infoFromJsonInput(@RequestBody List<Line2D> lines) {
        var chains = chainService.buildChains(lines);
        return chains.stream()
                .map(chainService::simplifyChain)
                // default comparator sorts ascending
                .sorted(Comparator.comparingDouble(SimplifiedLineChain::length).reversed())
                .collect(Collectors.toList());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody List<SimplifiedLineChain> infoFromFileInput(@RequestPart("file") MultipartFile file) throws InputFileException {
        if (file == null || file.isEmpty()) {
            throw new EmptyInputFileException();
        }

        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            var sb = new StringBuilder();
            reader.lines().forEach(line -> sb.append(line).append("\n"));
            return infoFromStringInput(sb.toString());
        } catch (IOException e) {
            throw new UnreadableInputFileException();
        }
    }
}
