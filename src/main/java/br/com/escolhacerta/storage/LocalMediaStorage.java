package br.com.escolhacerta.storage;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;

@Component @ConditionalOnProperty(name="app.storage.type",havingValue="local",matchIfMissing=true) public class LocalMediaStorage implements MediaStorage {
    private final Path root;
    public LocalMediaStorage(@Value("${app.storage.local-path}")String root) {
        this.root=Path.of(root).toAbsolutePath().normalize();
    }
    public String store(byte[] data,String ext,String type) {
        try {
            Files.createDirectories(root);
            String key=java.util.UUID.randomUUID()+"."+ext;
            Files.write(root.resolve(key),data,StandardOpenOption.CREATE_NEW);
            return "/media/"+key;
        }
        catch(java.io.IOException e) {
            throw new IllegalStateException("Não foi possível salvar a imagem",e);
        }
    }
}
