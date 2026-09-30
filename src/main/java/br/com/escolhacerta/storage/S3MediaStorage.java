package br.com.escolhacerta.storage;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.core.sync.RequestBody;

@Component @ConditionalOnProperty(name="app.storage.type",havingValue="s3") public class S3MediaStorage implements MediaStorage,AutoCloseable {
    private final S3Client client;
    private final String bucket,base;
    public S3MediaStorage(@Value("${app.storage.bucket}")String bucket,@Value("${app.storage.public-base-url}")String base,@Value("${app.storage.region}")String region,@Value("${app.storage.endpoint:}")String endpoint) {
        if(bucket.isBlank()||!base.startsWith("https://"))throw new IllegalArgumentException("Configure bucket e public-base-url HTTPS");
        var builder=S3Client.builder().region(Region.of(region));
        if(!endpoint.isBlank())builder.endpointOverride(java.net.URI.create(endpoint)).forcePathStyle(true);
        client=builder.build();
        this.bucket=bucket;
        this.base=base.replaceAll("/+$","");
    }
    public String store(byte[] data,String ext,String type) {
        String key="media/"+java.util.UUID.randomUUID()+"."+ext;
        client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).contentType(type).build(),RequestBody.fromBytes(data));
        return base+"/"+key;
    }
    public void close() {
        client.close();
    }
}
