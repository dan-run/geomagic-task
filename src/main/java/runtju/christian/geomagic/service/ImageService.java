package runtju.christian.geomagic.service;

import org.springframework.stereotype.Service;
import runtju.christian.geomagic.dto.LineChain;
import runtju.christian.geomagic.exceptions.ImageCreationException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

@Service
public class ImageService {
    // colors clamped for better visibility
    private final int COLOR_BOUND_LOW = 20;
    private final int COLOR_BOUND_HIGH = 230;

    private final int IMAGE_SIZE = 1024;

    public byte[] buildImageFromChains(List<LineChain> chains) throws ImageCreationException {
        var img = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);

        var colors = generateColors(chains.size());
        var imgGraphics = img.createGraphics();
        imgGraphics.setStroke(new BasicStroke(3));

        for (var chain : chains) {
            var color = colors.pop();
            imgGraphics.setColor(color);

            for (var line : chain.getLines()) {
                imgGraphics.drawLine(line.A().x(), line.A().y(), line.B().x(), line.B().y());
            }
        }
        imgGraphics.dispose();

        try (var baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "jpeg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ImageCreationException(e);
        }
    }

    /**
     * Naive RBG rotation to create distinguishable colors.
     *
     * @param size how many colors to create
     * @return a stack of different colors of the requested size
     */
    private Stack<Color> generateColors(int size) {
        var stack = new Stack<Color>();
        if (size == 0) return stack;

        int stepSize = (COLOR_BOUND_HIGH - COLOR_BOUND_LOW) / size;

        int value = COLOR_BOUND_LOW;
        int pos = 0;
        while (stack.size() < size) {
            switch (pos % 3) {
                case 0:
                    stack.push(new Color(value, COLOR_BOUND_LOW, COLOR_BOUND_LOW));
                    break;
                case 1:
                    stack.push(new Color(COLOR_BOUND_LOW, value, COLOR_BOUND_LOW));
                    break;
                case 2:
                    stack.push(new Color(COLOR_BOUND_LOW, COLOR_BOUND_LOW, value));
                    break;
            }
            value += stepSize;
            pos++;
        }
        return stack;
    }
}
