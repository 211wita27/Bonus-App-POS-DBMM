package at.htlle.service;

import at.htlle.dto.AccountResponse;
import at.htlle.dto.LedgerEntryResponse;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.entity.PointLedger;
import at.htlle.repository.LoyaltyAccountRepository;
import at.htlle.repository.PointLedgerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Builds detailed account views including ledger details.
 */
@Service
public class AccountQueryService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final PointLedgerRepository pointLedgerRepository;

    public AccountQueryService(LoyaltyAccountRepository loyaltyAccountRepository,
                               PointLedgerRepository pointLedgerRepository) {
        this.loyaltyAccountRepository = loyaltyAccountRepository;
        this.pointLedgerRepository = pointLedgerRepository;
    }

    /**
     * Loads a single loyalty account and maps it to a response.
     *
     * @param accountId loyalty account id
     * @param includeLedger whether to include ledger history
     * @return account response model
     */
    public AccountResponse getAccountResponse(Long accountId, boolean includeLedger) {
        LoyaltyAccount account = loyaltyAccountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        return buildAccountResponse(account, includeLedger);
    }

    /**
     * Converts an account entity to a response DTO.
     *
     * @param account loyalty account entity
     * @param includeLedger whether to include ledger entries
     * @return account response model
     */
    public AccountResponse buildAccountResponse(LoyaltyAccount account, boolean includeLedger) {
        List<LedgerEntryResponse> ledgerEntries = null;
        if (includeLedger) {
            ledgerEntries = pointLedgerRepository.findDetailedByAccountIdOrderByOccurredAtDesc(account.getId())
                    .stream()
                    .map(this::toLedgerEntryResponse)
                    .collect(Collectors.toList());
        }

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomer().getId(),
                account.getCustomer().getFirstName(),
                account.getCustomer().getLastName(),
                account.getRestaurant().getId(),
                account.getRestaurant().getName(),
                account.getStatus(),
                account.getTier(),
                account.getCurrentPoints(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                ledgerEntries);
    }

    private LedgerEntryResponse toLedgerEntryResponse(PointLedger entry) {
        String redemptionCode = null;
        if (entry.getEntryType() == PointLedger.EntryType.REDEEM && entry.getRedemption() != null) {
            redemptionCode = entry.getRedemption().getRedemptionCode();
        }
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getEntryType(),
                entry.getPoints(),
                entry.getBalanceAfter(),
                entry.getOccurredAt(),
                entry.getDescription(),
                entry.getPurchase() != null ? entry.getPurchase().getId() : null,
                entry.getPointRule() != null ? entry.getPointRule().getId() : null,
                entry.getRedemption() != null ? entry.getRedemption().getId() : null,
                redemptionCode);
    }
}
