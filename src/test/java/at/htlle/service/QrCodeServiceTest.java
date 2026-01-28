package at.htlle.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeService();

    @Test
    void generateDataUriReturnsPngData() {
        String dataUri = qrCodeService.generateDataUri("BONUSAPP|REDEEM|TEST", 160);

        assertThat(dataUri).startsWith("data:image/png;base64,");
        String base64 = dataUri.substring("data:image/png;base64,".length());
        byte[] decoded = Base64.getDecoder().decode(base64);

        // PNG header bytes: 89 50 4E 47 0D 0A 1A 0A
        assertThat(decoded).hasSizeGreaterThan(8);
        assertThat(decoded[0]).isEqualTo((byte) 0x89);
        assertThat(decoded[1]).isEqualTo((byte) 0x50);
        assertThat(decoded[2]).isEqualTo((byte) 0x4E);
        assertThat(decoded[3]).isEqualTo((byte) 0x47);
    }
}
