package br.com.escolhacerta.service;
import br.com.escolhacerta.dto.PublicFormLink;
import br.com.escolhacerta.repository.CustomFormRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class PublicFormMenu {
    private final CustomFormRepository repo;
    private List<PublicFormLink> cached = List.of();
    private long expires;
    public PublicFormMenu(CustomFormRepository repo) { this.repo = repo; }
    public synchronized List<PublicFormLink> links() {
        if (System.currentTimeMillis() >= expires) {
            cached = List.copyOf(repo.findTop20ByPublishedTrueOrderByIdDesc());
            expires = System.currentTimeMillis() + 30000;
        }
        return cached;
    }
    public void invalidate() {
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void afterCommit() { clear(); }
                });
        } else clear();
    }
    private synchronized void clear() { expires = 0; }
}
