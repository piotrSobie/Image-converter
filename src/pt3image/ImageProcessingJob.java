package pt3image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javax.imageio.ImageIO;

public class ImageProcessingJob extends Task<Void>{
    private File originalFile;
    private File outputDir;
    
    public ImageProcessingJob(File file){
        originalFile = file;
        updateMessage("Czeka");
    }
    
    public File getFile(){
        return originalFile;
    }
    
    public void setOutputDir(File file){
        outputDir = file;
    }
    
    @Override
    protected Void call() throws Exception {
        updateMessage("Trwa");
        updateProgress(0, 1);
        long start = System.currentTimeMillis();
        convertToGrayscale(originalFile, outputDir);
        
        updateMessage("Zakonczono w " + (System.currentTimeMillis() - start) + " ms");
        
        return null;
    }
    
    private void convertToGrayscale(
            File originalFile, //oryginalny plik graficzny
            File outputDir //katalog docelowy
    ){
        try{
            //wczytanie oryginalnego pliku do pamieci
            BufferedImage original = ImageIO.read(originalFile);
            
            //przygotowanie bufora na grafike w skali szarosci
            BufferedImage grayscale = new BufferedImage(
                original.getWidth(), original.getHeight(), original.getType());
            
            //przetwarzanie piksel po pikselu
            for(int i = 0; i < original.getWidth(); i++){
                for(int j = 0; j < original.getHeight(); j++){
                    //pobranie skladowych RGB
                    int red = new Color(original.getRGB(i, j)).getRed();
                    int green = new Color(original.getRGB(i, j)).getGreen();
                    int blue = new Color(original.getRGB(i, j)).getBlue();
                    //obliczenie jasnosci piksela dla obrazu w skali szarosci
                    int luminosity = (int) (0.21*red + 0.71*green + 0.07*blue);
                    //przygotowanie wartosci koloru w oparciu o obliczona jasnosc
                    int newPixel = new Color(luminosity, luminosity, luminosity).getRGB();
                    //zapisanie nowego piksela w buforze
                    grayscale.setRGB(i, j, newPixel);
                }
                //obliczenie postepu przetwarzania jako liczny z przedzialu [0, 1]
                double progress = (1.0 + i) / original.getWidth();
                //aktualizacja wlasnosci zbindowanej z paskiem postepu w tabeli
                Platform.runLater(() -> updateProgress(progress, 1));
            }
            //przygotowanie sciezki wskazujacej na plik wynikowy
            Path outputPath = Paths.get(outputDir.getAbsolutePath(), originalFile.getName());
            //zapisanie zawartosci bufora do pliku na dysku
            ImageIO.write(grayscale, "jpg", outputPath.toFile());
        }
        catch(IOException ex){
        //translacja wyjatku
        updateMessage("Blad");
        updateProgress(0, 1);
        throw new RuntimeException(ex);
        }
    }
    
}
