package br.com.escolhacerta.storage;

public interface MediaStorage {
    String store(byte[] data,String extension,String contentType);
}
