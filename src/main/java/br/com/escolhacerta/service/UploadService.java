package br.com.escolhacerta.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import br.com.escolhacerta.storage.MediaStorage;

@Service public class UploadService {
    private final MediaStorage storage;
    public UploadService(MediaStorage storage) {
        this.storage=storage;
    }
    public String upload(MultipartFile file) {
        if(file.isEmpty()||file.getSize()>5*1024*1024)throw new IllegalArgumentException("Envie uma imagem de até 5 MB");
        try(var stream=javax.imageio.ImageIO.createImageInputStream(file.getInputStream())) {
            var readers=javax.imageio.ImageIO.getImageReaders(stream);
            if(!readers.hasNext())throw new IllegalArgumentException("Use uma imagem PNG ou JPEG");
            var reader=readers.next();
            try {
                reader.setInput(stream);
                String format=reader.getFormatName().toLowerCase();
                if(!java.util.Set.of("png","jpeg","jpg").contains(format))throw new IllegalArgumentException("Use PNG ou JPEG");
                int width=reader.getWidth(0),height=reader.getHeight(0);
                if(width>3000||height>3000||(long)width*height>4000000)throw new IllegalArgumentException("Imagem grande demais. Use até 3000 pixels por lado e 4 megapixels");
                var image=reader.read(0);
                String ext=format.equals("png")?"png":"jpg";
                var output=new java.io.ByteArrayOutputStream();
                if(!javax.imageio.ImageIO.write(image,ext,output))throw new IllegalArgumentException("Não foi possível processar a imagem");
                if(output.size()>5*1024*1024)throw new IllegalArgumentException("A imagem processada ultrapassa 5 MB");
                return storage.store(output.toByteArray(),ext,"image/"+(ext.equals("jpg")?"jpeg":"png"));
            }
            finally {
                reader.dispose();
            }
        }
        catch(java.io.IOException e) {
            throw new IllegalArgumentException("Não foi possível ler a imagem");
        }
    }
}
